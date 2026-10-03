package kr.hqservice.framework.bukkit.core.coroutine.dispatcher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TicksForTest {
    @Test
    fun `delays are rounded up to whole ticks so they never end early`() {
        assertEquals(1L, ticksFor(1))
        assertEquals(1L, ticksFor(50))
        assertEquals(2L, ticksFor(51))
        assertEquals(2L, ticksFor(99))
        assertEquals(3L, ticksFor(101))
        assertEquals(1L, ticksFor(0))
    }
}
