package kr.hqservice.framework.database.redis

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.time.Duration

class RedisStore<T : Any> internal constructor(
    private val commands: KeyValueCommands,
    private val json: Json,
    private val serializer: KSerializer<T>,
    private val prefix: String,
    private val ttl: Duration?,
) {
    suspend fun get(id: String): T? = commands.get(key(id))?.let(::decode)

    suspend fun set(id: String, value: T) {
        commands.set(key(id), encode(value), ttl)
    }

    suspend fun delete(id: String): Boolean = commands.delete(key(id))

    suspend fun exists(id: String): Boolean = commands.exists(key(id))

    suspend fun touch(id: String): Boolean {
        val ttl = ttl ?: return false
        return commands.expire(key(id), ttl)
    }

    suspend fun update(id: String, block: (T?) -> T?): T? {
        val key = key(id)
        repeat(MAX_UPDATE_ATTEMPTS) {
            val current = commands.get(key)
            val next = block(current?.let(::decode))
            if (commands.compareAndSet(key, current, next?.let(::encode), ttl)) return next
        }
        throw IllegalStateException("concurrent update on $key")
    }

    fun key(id: String): String = "$prefix:$id"

    private fun encode(value: T): ByteArray = json.encodeToString(serializer, value).toByteArray()

    private fun decode(bytes: ByteArray): T = json.decodeFromString(serializer, bytes.decodeToString())

    private companion object {
        const val MAX_UPDATE_ATTEMPTS = 5
    }
}

class RedisStores(private val provider: RedisProvider, private val json: Json) {
    fun <T : Any> create(serializer: KSerializer<T>, prefix: String, ttl: Duration? = null): RedisStore<T> {
        check(provider.enabled) { "redis.uri is not configured" }
        require(ttl == null || ttl.toMillis() > 0) { "ttl must be at least one millisecond" }
        return RedisStore(LettuceKeyValueCommands(provider), json, serializer, prefix, ttl)
    }
}

inline fun <reified T : Any> RedisStores.create(prefix: String, ttl: Duration? = null): RedisStore<T> =
    create(serializer<T>(), prefix, ttl)
