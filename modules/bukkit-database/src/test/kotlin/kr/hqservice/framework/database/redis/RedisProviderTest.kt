package kr.hqservice.framework.database.redis

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.InetAddress
import java.net.ServerSocket
import java.time.Duration
import java.util.logging.Logger

class RedisProviderTest {
    private val disabled = RedisSettings("", "hq", Duration.ofSeconds(3600))

    @Test
    fun `disabled provider refuses connections`() {
        val provider = RedisProvider(disabled, mockk(relaxed = true))
        assertFalse(provider.enabled)
        val error = assertThrows<IllegalStateException> { provider.connection() }
        assertEquals("redis.uri is not configured", error.message)
        assertThrows<IllegalStateException> { provider.pubSubConnection() }
    }

    @Test
    fun `invalid uri fails when the provider is created`() {
        val error = assertThrows<IllegalStateException> {
            RedisProvider(RedisSettings("not a redis uri", "hq", Duration.ofSeconds(1)), mockk(relaxed = true))
        }
        assertTrue(error.message!!.contains("redis.uri"), error.message)
    }

    @Test
    fun `connecting to a server that never answers fails within the timeout`() {
        ServerSocket(0, 50, InetAddress.getLoopbackAddress()).use { silent ->
            val provider = RedisProvider(RedisSettings("redis://127.0.0.1:${silent.localPort}", "hq", Duration.ofSeconds(1)), mockk(relaxed = true))
            try {
                assertTimeoutPreemptively(Duration.ofSeconds(10)) {
                    assertThrows<Exception> { provider.connection() }
                }
            } finally {
                provider.close()
            }
        }
    }

    @Test
    fun `closing a never opened provider does nothing`() {
        val logger = mockk<Logger>()
        val provider = RedisProvider(RedisSettings("redis://127.0.0.1:1", "hq", Duration.ofSeconds(1)), logger)
        assertDoesNotThrow {
            provider.close()
            provider.close()
        }
    }
}
