package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.coroutine.extension.runCatchingCancellable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RunCatchingCancellableTest {
    @Test
    fun `ordinary failures are captured but cancellation propagates`() = runBlocking {
        val failed = runCatchingCancellable { error("boom") }
        assertTrue(failed.isFailure)
        assertEquals("boom", failed.exceptionOrNull()!!.message)

        assertEquals(3, runCatchingCancellable { 3 }.getOrThrow())

        assertThrows<CancellationException> { runCatchingCancellable { throw CancellationException("stop") } }
        Unit
    }
}
