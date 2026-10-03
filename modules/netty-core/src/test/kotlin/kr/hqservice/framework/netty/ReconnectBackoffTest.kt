package kr.hqservice.framework.netty

import kr.hqservice.framework.netty.bootstrap.ReconnectBackoff
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReconnectBackoffTest {
    @Test
    fun `delay doubles from one second up to the cap and resets after a successful connection`() {
        val backoff = ReconnectBackoff(baseMillis = 1000, maxMillis = 30_000, jitter = { 0.0 })

        assertEquals(listOf(1000L, 2000L, 4000L, 8000L, 16_000L, 30_000L, 30_000L), List(7) { backoff.nextDelayMillis() })

        backoff.reset()
        assertEquals(1000L, backoff.nextDelayMillis())
    }

    @Test
    fun `jitter spreads the delay within twenty percent`() {
        val low = ReconnectBackoff(baseMillis = 1000, maxMillis = 30_000, jitter = { 0.0 }).nextDelayMillis()
        val high = ReconnectBackoff(baseMillis = 1000, maxMillis = 30_000, jitter = { 1.0 }).nextDelayMillis()

        assertEquals(1000L, low)
        assertTrue(high in 1100L..1200L, "high=$high")
    }
}
