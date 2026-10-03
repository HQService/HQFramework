package kr.hqservice.framework.database.redis

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class RedisStoreTest {
    @Serializable
    data class Party(val leader: String, val members: List<String>)

    private val commands = InMemoryKeyValueCommands()
    private val ttl = Duration.ofMinutes(30)
    private val store = RedisStore(commands, Json, Party.serializer(), "myplugin:party", ttl)
    private val persistent = RedisStore(commands, Json, Party.serializer(), "myplugin:persistent", null)

    @Test
    fun `key joins prefix and id`() {
        assertEquals("myplugin:party:p1", store.key("p1"))
    }

    @Test
    fun `set and get round trip`() = runTest {
        assertNull(store.get("p1"))

        store.set("p1", Party("a", listOf("a", "b")))

        assertEquals(Party("a", listOf("a", "b")), store.get("p1"))
        assertEquals("""{"leader":"a","members":["a","b"]}""", commands.values["myplugin:party:p1"]!!.decodeToString())
    }

    @Test
    fun `delete and exists`() = runTest {
        store.set("p1", Party("a", emptyList()))
        assertTrue(store.exists("p1"))

        assertTrue(store.delete("p1"))

        assertFalse(store.exists("p1"))
        assertFalse(store.delete("p1"))
    }

    @Test
    fun `ttl is applied on set and touch`() = runTest {
        store.set("p1", Party("a", emptyList()))
        assertEquals(ttl, commands.ttls["myplugin:party:p1"])

        commands.ttls.clear()
        assertTrue(store.touch("p1"))
        assertEquals(ttl, commands.ttls["myplugin:party:p1"])
        assertFalse(store.touch("missing"))
    }

    @Test
    fun `store without ttl never expires and touch does nothing`() = runTest {
        persistent.set("p1", Party("a", emptyList()))

        assertNull(commands.ttls["myplugin:persistent:p1"])
        assertFalse(persistent.touch("p1"))
    }

    @Test
    fun `update creates when absent`() = runTest {
        val result = store.update("p1") { current: Party? -> current ?: Party("a", listOf("a")) }

        assertEquals(Party("a", listOf("a")), result)
        assertEquals(Party("a", listOf("a")), store.get("p1"))
        assertEquals(ttl, commands.ttls["myplugin:party:p1"])
    }

    @Test
    fun `update returning null deletes`() = runTest {
        store.set("p1", Party("a", listOf("a")))

        assertNull(store.update("p1") { null })

        assertFalse(store.exists("p1"))
    }

    @Test
    fun `update retries when the value changes before compare and set`() = runTest {
        store.set("p1", Party("a", listOf("a")))
        commands.beforeCompareAndSet = { key ->
            commands.values[key] = Json.encodeToString(Party.serializer(), Party("a", listOf("a", "x"))).toByteArray()
            commands.beforeCompareAndSet = {}
        }
        var invocations = 0

        val result = store.update("p1") { current: Party? ->
            invocations++
            current!!.copy(members = current.members + "b")
        }

        assertEquals(Party("a", listOf("a", "x", "b")), result)
        assertEquals(Party("a", listOf("a", "x", "b")), store.get("p1"))
        assertEquals(2, invocations)
    }

    @Test
    fun `update gives up after five conflicts`() = runTest {
        var round = 0
        commands.beforeCompareAndSet = { key ->
            commands.values[key] = Json.encodeToString(Party.serializer(), Party("rival-${round++}", emptyList())).toByteArray()
        }

        val error = assertThrows<IllegalStateException> { store.update("p1") { Party("a", emptyList()) } }

        assertEquals("concurrent update on myplugin:party:p1", error.message)
        assertEquals(5, commands.compareAndSetCalls)
    }

    @Test
    fun `stores refuse to create when redis is disabled`() {
        val stores = RedisStores(RedisProvider(RedisSettings("", "hq", Duration.ofSeconds(60)), mockk(relaxed = true)), Json)

        val error = assertThrows<IllegalStateException> { stores.create<Party>("myplugin:party") }

        assertEquals("redis.uri is not configured", error.message)
    }

    @Test
    fun `stores reject a ttl shorter than a millisecond`() {
        RedisProvider(RedisSettings("redis://127.0.0.1:6379", "hq", Duration.ofSeconds(60)), mockk(relaxed = true)).use { provider ->
            val stores = RedisStores(provider, Json)

            assertThrows<IllegalArgumentException> { stores.create(Party.serializer(), "myplugin:party", Duration.ZERO) }
            assertEquals("myplugin:party:p1", stores.create<Party>("myplugin:party", Duration.ofMinutes(1)).key("p1"))
        }
    }
}
