package kr.hqservice.framework.database.redis

import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
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
        val messenger = RedisMessenger(LettucePubSubTransport(provider), settings, Json, mockk(relaxed = true))
        val received = CompletableFuture<Ping>()
        val subscription = messenger.subscribe("ping", Ping.serializer()) { received.complete(it) }
        try {
            messenger.publish("ping", Ping.serializer(), Ping("hello"))
            assertEquals(Ping("hello"), received.get(2, TimeUnit.SECONDS))
        } finally {
            subscription.close()
        }
    }
}
