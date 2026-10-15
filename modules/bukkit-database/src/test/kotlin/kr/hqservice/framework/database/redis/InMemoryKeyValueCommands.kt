package kr.hqservice.framework.database.redis

import java.time.Duration

class InMemoryKeyValueCommands : KeyValueCommands {
    val values = mutableMapOf<String, ByteArray>()
    val ttls = mutableMapOf<String, Duration>()
    var beforeCompareAndSet: (String) -> Unit = {}
    var compareAndSetCalls = 0

    override suspend fun get(key: String): ByteArray? = values[key]

    override suspend fun set(key: String, value: ByteArray, ttl: Duration?) {
        values[key] = value
        if (ttl == null) ttls.remove(key) else ttls[key] = ttl
    }

    override suspend fun delete(key: String): Boolean {
        ttls.remove(key)
        return values.remove(key) != null
    }

    override suspend fun exists(key: String): Boolean = key in values

    override suspend fun expire(key: String, ttl: Duration): Boolean {
        if (key !in values) return false
        ttls[key] = ttl
        return true
    }

    override suspend fun compareAndSet(key: String, expected: ByteArray?, value: ByteArray?, ttl: Duration?): Boolean {
        compareAndSetCalls++
        beforeCompareAndSet(key)
        val current = values[key]
        val matches = if (expected == null) current == null else current != null && current.contentEquals(expected)
        if (!matches) return false
        if (value == null) delete(key) else set(key, value, ttl)
        return true
    }
}
