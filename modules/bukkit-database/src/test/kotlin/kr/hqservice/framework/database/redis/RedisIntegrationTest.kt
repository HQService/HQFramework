package kr.hqservice.framework.database.redis

import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kr.hqservice.framework.database.repository.player.cache.LettucePlayerDataCache
import kr.hqservice.framework.database.repository.player.cache.OwnerFence
import kr.hqservice.framework.database.repository.player.session.AcquireResult
import kr.hqservice.framework.database.repository.player.session.redis.LettuceSessionStore
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisIntegrationTest {
    @Serializable
    data class Ping(val text: String)

    private lateinit var provider: RedisProvider
    private lateinit var settings: RedisSettings

    @BeforeAll
    fun connect() {
        val uri = System.getenv("HQ_TEST_REDIS_URI")
        Assumptions.assumeTrue(uri != null)
        settings = RedisSettings(uri, "hq-test-${UUID.randomUUID()}", Duration.ofSeconds(60))
        provider = RedisProvider(settings, mockk(relaxed = true))
    }

    @AfterAll
    fun close() {
        if (::provider.isInitialized) provider.close()
    }

    @Test
    fun `provider connects`() {
        assertTrue(provider.enabled)
        assertEquals("PONG", provider.connection().sync().ping())
    }

    @Test
    fun `messenger round trips over lettuce`() {
        val messenger = RedisMessenger(LettucePubSubTransport(provider, mockk(relaxed = true)), settings, Json, mockk(relaxed = true))
        val received = CompletableFuture<Ping>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        messenger.subscribe("ping", Ping.serializer(), scope) { received.complete(it) }
        try {
            messenger.publish("ping", Ping.serializer(), Ping("hello"))
            assertEquals(Ping("hello"), received.get(2, TimeUnit.SECONDS))
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `lettuce session store acquires commits and releases`() = runBlocking {
        val store = LettuceSessionStore(provider)
        val key = settings.key("session", UUID.randomUUID().toString())

        assertEquals(AcquireResult.Acquired(0), store.acquire(key, "a", 30_000))
        assertEquals(AcquireResult.Held("a"), store.acquire(key, "b", 30_000))
        assertEquals(1L, store.commit(key, "a", 0, 30_000))
        assertNull(store.commit(key, "a", 0, 30_000))
        assertNull(store.commit(key, "b", 1, 30_000))
        assertEquals(AcquireResult.Acquired(1), store.acquire(key, "a", 30_000))
        assertTrue(store.verify(key, "a", 1))
        assertFalse(store.verify(key, "a", 0))
        assertFalse(store.verify(key, "b", 1))
        val leaseBefore = leaseUntil(key)
        store.renew(listOf(key), "a", 60_000)
        assertTrue(leaseUntil(key) >= leaseBefore + 20_000)
        assertTrue(provider.connection().sync().pttl(key) > Duration.ofDays(29).toMillis())
        assertFalse(store.release(key, "b"))
        assertTrue(store.release(key, "a"))
        assertNull(provider.connection().sync().hget(key, "owner"))
        assertEquals(AcquireResult.Acquired(1), store.acquire(key, "b", 30_000))
        assertTrue(store.release(key, "b"))
    }

    private fun leaseUntil(key: String): Long = provider.connection().sync().hget(key, "lease_until")!!.decodeToString().toLong()

    @Test
    fun `lettuce session store lease expires`() = runBlocking {
        val store = LettuceSessionStore(provider)
        val key = settings.key("session", UUID.randomUUID().toString())

        store.acquire(key, "a", 100)
        assertEquals(1L, store.commit(key, "a", 0, 100))
        delay(300)

        assertEquals(AcquireResult.Acquired(1), store.acquire(key, "b", 30_000))
        assertNull(store.commit(key, "a", 1, 30_000))
        assertTrue(store.release(key, "b"))
    }

    @Test
    fun `lettuce player data cache writes reads expires and deletes`() = runBlocking {
        val cache = LettucePlayerDataCache(provider)
        val key = settings.key("data", "test", UUID.randomUUID().toString())

        assertTrue(cache.write(key, "first".toByteArray(), null, null))
        assertEquals("first", cache.read(key)?.decodeToString())
        assertEquals(-1L, provider.connection().sync().pttl(key))

        assertTrue(cache.write(key, "ttl".toByteArray(), Duration.ofSeconds(30), null))
        assertTrue(provider.connection().sync().pttl(key) in 1..30_000)
        cache.persist(key)
        assertEquals(-1L, provider.connection().sync().pttl(key))

        assertTrue(cache.write(key, "second".toByteArray(), null, null))
        assertEquals("second", cache.read(key)?.decodeToString())
        assertEquals(-1L, provider.connection().sync().pttl(key))

        cache.delete(key)
        assertNull(cache.read(key))
    }

    @Test
    fun `lettuce player data cache honours the owner fence`() = runBlocking {
        val cache = LettucePlayerDataCache(provider)
        val store = LettuceSessionStore(provider)
        val sessionKey = settings.key("session", UUID.randomUUID().toString())
        val key = settings.key("data", "test", UUID.randomUUID().toString())
        store.acquire(sessionKey, "a", 30_000)

        assertTrue(cache.write(key, "mine".toByteArray(), Duration.ofSeconds(30), OwnerFence(sessionKey, "a")))
        assertTrue(provider.connection().sync().pttl(key) in 1..30_000)
        assertFalse(cache.write(key, "theirs".toByteArray(), null, OwnerFence(sessionKey, "b")))
        assertEquals("mine", cache.read(key)?.decodeToString())

        store.release(sessionKey, "a")
        assertFalse(cache.write(key, "released".toByteArray(), null, OwnerFence(sessionKey, "a")))
        cache.delete(key)
    }
}
