package kr.hqservice.framework.database.repository.player.cache

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryPlayerDataCache(private val sessionOwner: (String) -> String? = { null }) : PlayerDataCache {
    val values = ConcurrentHashMap<String, ByteArray>()
    val ttls = ConcurrentHashMap<String, Duration>()
    val writes = AtomicInteger()

    fun text(key: String): String? = values[key]?.decodeToString()

    override suspend fun read(key: String): ByteArray? = values[key]

    override suspend fun write(key: String, value: ByteArray, ttl: Duration?, fence: OwnerFence?): Boolean {
        if (fence != null && sessionOwner(fence.sessionKey) != fence.owner) return false
        writes.incrementAndGet()
        values[key] = value
        if (ttl == null) ttls.remove(key) else ttls[key] = ttl
        return true
    }

    override suspend fun persist(key: String) {
        ttls.remove(key)
    }

    override suspend fun delete(key: String) {
        values.remove(key)
        ttls.remove(key)
    }
}
