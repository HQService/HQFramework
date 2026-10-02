package kr.hqservice.framework.database.repository.player.session.redis

import kr.hqservice.framework.database.repository.player.session.AcquireResult

class InMemorySessionStore : SessionStore {
    class Entry(val owner: String, var version: Long, var expiresAt: Long)

    @Volatile
    var now: Long = 0
    private val entries = mutableMapOf<String, Entry>()

    fun entry(key: String): Entry? = synchronized(this) {
        val entry = entries[key] ?: return null
        if (entry.expiresAt <= now) {
            entries.remove(key)
            return null
        }
        entry
    }

    override suspend fun acquire(key: String, owner: String, leaseMillis: Long): AcquireResult = synchronized(this) {
        val current = entry(key)
        if (current != null && current.owner != owner) return AcquireResult.Held(current.owner)
        val acquired = Entry(owner, current?.version ?: 0, now + leaseMillis)
        entries[key] = acquired
        AcquireResult.Acquired(acquired.version)
    }

    override suspend fun renew(keys: List<String>, owner: String, leaseMillis: Long) = synchronized(this) {
        keys.mapNotNull(::entry).filter { it.owner == owner }.forEach { it.expiresAt = now + leaseMillis }
    }

    override suspend fun commit(key: String, owner: String, expectedVersion: Long, leaseMillis: Long): Long? = synchronized(this) {
        val current = entry(key)?.takeIf { it.owner == owner && it.version == expectedVersion } ?: return null
        current.version++
        current.expiresAt = now + leaseMillis
        current.version
    }

    override suspend fun release(key: String, owner: String): Boolean = synchronized(this) {
        if (entry(key)?.owner != owner) return false
        entries.remove(key)
        true
    }
}
