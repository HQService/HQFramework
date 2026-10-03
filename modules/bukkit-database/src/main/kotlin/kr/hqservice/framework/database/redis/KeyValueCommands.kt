package kr.hqservice.framework.database.redis

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import kotlinx.coroutines.future.await
import java.time.Duration

interface KeyValueCommands {
    suspend fun get(key: String): ByteArray?

    suspend fun set(key: String, value: ByteArray, ttl: Duration?)

    suspend fun delete(key: String): Boolean

    suspend fun exists(key: String): Boolean

    suspend fun expire(key: String, ttl: Duration): Boolean

    suspend fun compareAndSet(key: String, expected: ByteArray?, value: ByteArray?, ttl: Duration?): Boolean
}

class LettuceKeyValueCommands(private val provider: RedisProvider) : KeyValueCommands {
    private fun commands() = provider.connection().async()

    override suspend fun get(key: String): ByteArray? = commands().get(key).await()

    override suspend fun set(key: String, value: ByteArray, ttl: Duration?) {
        if (ttl == null) commands().set(key, value).await() else commands().set(key, value, SetArgs.Builder.px(ttl.toMillis())).await()
    }

    override suspend fun delete(key: String): Boolean = commands().del(key).await() > 0

    override suspend fun exists(key: String): Boolean = commands().exists(key).await() > 0

    override suspend fun expire(key: String, ttl: Duration): Boolean = commands().pexpire(key, ttl.toMillis()).await()

    override suspend fun compareAndSet(key: String, expected: ByteArray?, value: ByteArray?, ttl: Duration?): Boolean =
        commands().eval<Long>(
            COMPARE_AND_SET,
            ScriptOutputType.INTEGER,
            arrayOf(key),
            expected ?: EMPTY,
            value ?: EMPTY,
            (ttl?.toMillis() ?: 0L).toString().toByteArray(),
            flag(expected == null),
            flag(value == null),
        ).await() == 1L

    private companion object {
        val EMPTY = ByteArray(0)

        fun flag(set: Boolean): ByteArray = (if (set) "1" else "0").toByteArray()

        const val COMPARE_AND_SET = """
            local current = redis.call('GET', KEYS[1])
            if ARGV[4] == '1' then
                if current then
                    return 0
                end
            elseif current ~= ARGV[1] then
                return 0
            end
            if ARGV[5] == '1' then
                redis.call('DEL', KEYS[1])
            elseif tonumber(ARGV[3]) > 0 then
                redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
            else
                redis.call('SET', KEYS[1], ARGV[2])
            end
            return 1
        """
    }
}
