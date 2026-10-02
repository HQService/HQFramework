package kr.hqservice.framework.database.repository.player.session.redis

import io.lettuce.core.ScriptOutputType
import kotlinx.coroutines.future.await
import kr.hqservice.framework.database.redis.RedisProvider
import kr.hqservice.framework.database.repository.player.session.AcquireResult

class LettuceSessionStore(private val provider: RedisProvider) : SessionStore {
    override suspend fun acquire(key: String, owner: String, leaseMillis: Long): AcquireResult {
        val reply = eval<List<ByteArray>>(ACQUIRE, ScriptOutputType.MULTI, listOf(key), owner, leaseMillis.toString())
        val value = reply[1].decodeToString()
        return if (reply[0].decodeToString() == "1") AcquireResult.Acquired(value.toLong()) else AcquireResult.Held(value)
    }

    override suspend fun renew(keys: List<String>, owner: String, leaseMillis: Long) {
        if (keys.isEmpty()) return
        eval<Long>(RENEW, ScriptOutputType.INTEGER, keys, owner, leaseMillis.toString())
    }

    override suspend fun commit(key: String, owner: String, expectedVersion: Long, leaseMillis: Long): Long? =
        eval<Long>(COMMIT, ScriptOutputType.INTEGER, listOf(key), owner, expectedVersion.toString(), leaseMillis.toString())
            .takeIf { it >= 0 }

    override suspend fun release(key: String, owner: String): Boolean =
        eval<Long>(RELEASE, ScriptOutputType.INTEGER, listOf(key), owner) == 1L

    private suspend fun <T> eval(script: String, type: ScriptOutputType, keys: List<String>, vararg args: String): T =
        provider.connection().async()
            .eval<T>(script, type, keys.toTypedArray(), *args.map { it.toByteArray() }.toTypedArray())
            .await()

    private companion object {
        const val ACQUIRE = """
            local owner = redis.call('HGET', KEYS[1], 'owner')
            if owner == false or owner == ARGV[1] then
                redis.call('HSET', KEYS[1], 'owner', ARGV[1])
                redis.call('HSETNX', KEYS[1], 'version', '0')
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return {'1', redis.call('HGET', KEYS[1], 'version')}
            end
            return {'0', owner}
        """

        const val RENEW = """
            for _, key in ipairs(KEYS) do
                if redis.call('HGET', key, 'owner') == ARGV[1] then
                    redis.call('PEXPIRE', key, ARGV[2])
                end
            end
            return 0
        """

        const val COMMIT = """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] and redis.call('HGET', KEYS[1], 'version') == ARGV[2] then
                local version = redis.call('HINCRBY', KEYS[1], 'version', 1)
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
                return version
            end
            return -1
        """

        const val RELEASE = """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
        """
    }
}
