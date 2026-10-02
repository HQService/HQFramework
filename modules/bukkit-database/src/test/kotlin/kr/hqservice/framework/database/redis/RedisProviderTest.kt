package kr.hqservice.framework.database.redis

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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
    fun `closing a never opened provider does nothing`() {
        val logger = mockk<Logger>()
        val provider = RedisProvider(RedisSettings("redis://127.0.0.1:1", "hq", Duration.ofSeconds(1)), logger)
        assertDoesNotThrow {
            provider.close()
            provider.close()
        }
    }
}
