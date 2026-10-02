package kr.hqservice.framework.database.redis

import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
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
        val subscription = messenger.subscribe("ping", Ping.serializer()) { received.complete(it) }
        try {
            messenger.publish("ping", Ping.serializer(), Ping("hello"))
            assertEquals(Ping("hello"), received.get(2, TimeUnit.SECONDS))
        } finally {
            subscription.close()
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
        store.renew(listOf(key), "a", 60_000)
        assertTrue(provider.connection().sync().pttl(key) > 30_000)
        assertFalse(store.release(key, "b"))
        assertTrue(store.release(key, "a"))
        assertEquals(AcquireResult.Acquired(0), store.acquire(key, "b", 30_000))
        assertTrue(store.release(key, "b"))
    }

    @Test
    fun `lettuce session store lease expires`() = runBlocking {
        val store = LettuceSessionStore(provider)
        val key = settings.key("session", UUID.randomUUID().toString())

        store.acquire(key, "a", 100)
        delay(300)

        assertEquals(AcquireResult.Acquired(0), store.acquire(key, "b", 30_000))
        assertTrue(store.release(key, "b"))
    }
}
