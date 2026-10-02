package kr.hqservice.framework.database.repository.player.cache

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import kotlinx.coroutines.future.await
import kr.hqservice.framework.database.redis.RedisProvider
import java.time.Duration

data class OwnerFence(val sessionKey: String, val owner: String)

interface PlayerDataCache {
    suspend fun read(key: String): ByteArray?

    suspend fun write(key: String, value: ByteArray, ttl: Duration?, fence: OwnerFence?): Boolean

    suspend fun persist(key: String)

    suspend fun delete(key: String)
}

class LettucePlayerDataCache(private val provider: RedisProvider) : PlayerDataCache {
    override suspend fun read(key: String): ByteArray? = provider.connection().async().get(key).await()

    override suspend fun write(key: String, value: ByteArray, ttl: Duration?, fence: OwnerFence?): Boolean {
        val commands = provider.connection().async()
        if (fence == null) {
            if (ttl == null) commands.set(key, value).await() else commands.set(key, value, SetArgs.Builder.px(ttl.toMillis())).await()
            return true
        }
        val ttlArgument = ttl?.toMillis()?.toString() ?: ""
        return commands.eval<Long>(
            FENCED_SET,
            ScriptOutputType.INTEGER,
            arrayOf(key, fence.sessionKey),
            value,
            fence.owner.toByteArray(),
            ttlArgument.toByteArray(),
        ).await() == 1L
    }

    override suspend fun persist(key: String) {
        provider.connection().async().persist(key).await()
    }

    override suspend fun delete(key: String) {
        provider.connection().async().del(key).await()
    }

    private companion object {
        const val FENCED_SET = """
            if redis.call('HGET', KEYS[2], 'owner') ~= ARGV[2] then
                return 0
            end
            if ARGV[3] == '' then
                redis.call('SET', KEYS[1], ARGV[1])
            else
                redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[3])
            end
            return 1
        """
    }
}
