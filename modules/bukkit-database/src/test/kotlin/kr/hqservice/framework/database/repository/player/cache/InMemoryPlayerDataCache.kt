package kr.hqservice.framework.database.repository.player.cache

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryPlayerDataCache : PlayerDataCache {
    val values = ConcurrentHashMap<String, ByteArray>()
    val ttls = ConcurrentHashMap<String, Duration>()
    val writes = AtomicInteger()

    fun text(key: String): String? = values[key]?.decodeToString()

    override suspend fun read(key: String): ByteArray? = values[key]

    override suspend fun write(key: String, value: ByteArray) {
        writes.incrementAndGet()
        values[key] = value
        ttls.remove(key)
    }

    override suspend fun expire(key: String, ttl: Duration) {
        if (values.containsKey(key)) ttls[key] = ttl
    }

    override suspend fun delete(key: String) {
        values.remove(key)
        ttls.remove(key)
    }
}
