package kr.hqservice.framework.database.repository.player.session.redis

import kr.hqservice.framework.database.repository.player.session.AcquireResult

class InMemorySessionStore : SessionStore {
    class Entry(var owner: String?, var version: Long?, var leaseUntil: Long?)

    @Volatile
    var now: Long = 0
    private val entries = mutableMapOf<String, Entry>()

    fun entry(key: String): Entry? = synchronized(this) { entries[key] }

    fun owner(key: String): String? = synchronized(this) { entries[key]?.owner }

    fun wipe(key: String) = synchronized(this) { entries.remove(key) }

    override suspend fun acquire(key: String, owner: String, leaseMillis: Long): AcquireResult = synchronized(this) {
        val current = entries[key]
        val holder = current?.owner
        if (holder != null && holder != owner && (current.leaseUntil ?: 0) >= now) return AcquireResult.Held(holder)
        val acquired = current ?: Entry(null, null, null).also { entries[key] = it }
        acquired.owner = owner
        acquired.leaseUntil = now + leaseMillis
        if (acquired.version == null) acquired.version = 0
        AcquireResult.Acquired(acquired.version!!)
    }

    override suspend fun renew(keys: List<String>, owner: String, leaseMillis: Long) = synchronized(this) {
        keys.forEach { key ->
            val current = entries[key]
            if (current == null) entries[key] = Entry(owner, null, now + leaseMillis)
            else if (current.owner == owner) current.leaseUntil = now + leaseMillis
        }
    }

    override suspend fun commit(key: String, owner: String, expectedVersion: Long, leaseMillis: Long): Long? = synchronized(this) {
        val current = entries.getOrPut(key) { Entry(owner, expectedVersion, null) }
        if (current.owner == owner && current.version == null) current.version = expectedVersion
        if (current.owner != owner || current.version != expectedVersion) return null
        current.version = expectedVersion + 1
        current.leaseUntil = now + leaseMillis
        current.version
    }

    override suspend fun verify(key: String, owner: String, expectedVersion: Long): Boolean = synchronized(this) {
        val current = entries.getOrPut(key) { Entry(owner, expectedVersion, null) }
        if (current.owner == owner && current.version == null) current.version = expectedVersion
        current.owner == owner && current.version == expectedVersion
    }

    override suspend fun ownedVersion(key: String, owner: String): Long? = synchronized(this) {
        entries[key]?.takeIf { it.owner == owner }?.version
    }

    override suspend fun release(key: String, owner: String): Boolean = synchronized(this) {
        val current = entries[key]?.takeIf { it.owner == owner } ?: return false
        current.owner = null
        current.leaseUntil = null
        true
    }
}
