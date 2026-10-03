package kr.hqservice.framework.database.repository.player.session.redis

import io.lettuce.core.ScriptOutputType
import kotlinx.coroutines.future.await
import kr.hqservice.framework.database.redis.RedisProvider
import kr.hqservice.framework.database.repository.player.session.AcquireResult
import java.time.Duration

class LettuceSessionStore(private val provider: RedisProvider) : SessionStore {
    override suspend fun acquire(key: String, owner: String, leaseMillis: Long): AcquireResult {
        val reply = eval<List<ByteArray>>(ACQUIRE, ScriptOutputType.MULTI, listOf(key), owner, leaseMillis.toString(), HOUSEKEEPING_TTL)
        val value = reply[1].decodeToString()
        return if (reply[0].decodeToString() == "1") AcquireResult.Acquired(value.toLong()) else AcquireResult.Held(value)
    }

    override suspend fun renew(keys: List<String>, owner: String, leaseMillis: Long) {
        if (keys.isEmpty()) return
        eval<Long>(RENEW, ScriptOutputType.INTEGER, keys, owner, leaseMillis.toString(), HOUSEKEEPING_TTL)
    }

    override suspend fun commit(key: String, owner: String, expectedVersion: Long, leaseMillis: Long): Long? =
        eval<Long>(COMMIT, ScriptOutputType.INTEGER, listOf(key), owner, expectedVersion.toString(), leaseMillis.toString(), HOUSEKEEPING_TTL)
            .takeIf { it >= 0 }

    override suspend fun release(key: String, owner: String): Boolean =
        eval<Long>(RELEASE, ScriptOutputType.INTEGER, listOf(key), owner, HOUSEKEEPING_TTL) == 1L

    override suspend fun verify(key: String, owner: String, expectedVersion: Long): Boolean =
        eval<Long>(VERIFY, ScriptOutputType.INTEGER, listOf(key), owner, expectedVersion.toString()) == 1L

    override suspend fun ownedVersion(key: String, owner: String): Long? =
        eval<Long>(OWNED_VERSION, ScriptOutputType.INTEGER, listOf(key), owner).takeIf { it >= 0 }

    private suspend fun <T> eval(script: String, type: ScriptOutputType, keys: List<String>, vararg args: String): T =
        provider.connection().async()
            .eval<T>(script, type, keys.toTypedArray(), *args.map { it.toByteArray() }.toTypedArray())
            .await()

    private companion object {
        val HOUSEKEEPING_TTL = Duration.ofDays(30).toMillis().toString()

        const val NOW = """
            local time = redis.call('TIME')
            local now = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)
        """

        const val ACQUIRE = NOW + """
            local owner = redis.call('HGET', KEYS[1], 'owner')
            local leaseUntil = tonumber(redis.call('HGET', KEYS[1], 'lease_until')) or 0
            if owner == false or owner == ARGV[1] or leaseUntil < now then
                redis.call('HSET', KEYS[1], 'owner', ARGV[1])
                redis.call('HSET', KEYS[1], 'lease_until', string.format('%d', now + tonumber(ARGV[2])))
                redis.call('HSETNX', KEYS[1], 'version', '0')
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
                return {'1', redis.call('HGET', KEYS[1], 'version')}
            end
            return {'0', owner}
        """

        const val RENEW = NOW + """
            local leaseUntil = string.format('%d', now + tonumber(ARGV[2]))
            for _, key in ipairs(KEYS) do
                if redis.call('HGET', key, 'owner') == ARGV[1] then
                    redis.call('HSET', key, 'lease_until', leaseUntil)
                    redis.call('PEXPIRE', key, ARGV[3])
                end
            end
            return 0
        """

        const val COMMIT = NOW + """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] and redis.call('HGET', KEYS[1], 'version') == ARGV[2] then
                local version = redis.call('HINCRBY', KEYS[1], 'version', 1)
                redis.call('HSET', KEYS[1], 'lease_until', string.format('%d', now + tonumber(ARGV[3])))
                redis.call('PEXPIRE', KEYS[1], ARGV[4])
                return version
            end
            return -1
        """

        const val VERIFY = """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] and redis.call('HGET', KEYS[1], 'version') == ARGV[2] then
                return 1
            end
            return 0
        """

        const val OWNED_VERSION = """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] then
                return tonumber(redis.call('HGET', KEYS[1], 'version'))
            end
            return -1
        """

        const val RELEASE = """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] then
                redis.call('HDEL', KEYS[1], 'owner', 'lease_until')
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return 1
            end
            return 0
        """
    }
}
