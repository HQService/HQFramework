package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class CoroutineTeardownTest {
    private val messages = mutableListOf<String>()
    private val logger = Logger.getLogger("CoroutineTeardownTest").apply {
        useParentHandlers = false
        addHandler(object : Handler() {
            override fun publish(record: LogRecord) { messages += record.message }
            override fun flush() {}
            override fun close() {}
        })
    }

    @Test
    fun `a child that ignores cancellation is reported as abandoned`() = runBlocking {
        val job = SupervisorJob()
        val scope = kotlinx.coroutines.CoroutineScope(job + Dispatchers.Default)
        scope.launch(CoroutineName("stuck")) { withContext(NonCancellable) { delay(60_000) } }

        CoroutineTeardown.awaitChildren(job, logger, gracePeriodMs = 100, forceCancelTimeoutMs = 100)

        assertTrue(messages.any { it.contains("Abandoning [stuck routine]") }, messages.toString())
        assertTrue(messages.any { it.contains("Cleaned up 0/1") }, messages.toString())
        job.cancel()
    }

    @Test
    fun `a cooperative child is cancelled and counted as finished`() = runBlocking {
        val job = SupervisorJob()
        val scope = kotlinx.coroutines.CoroutineScope(job + Dispatchers.Default)
        scope.launch(CoroutineName("loop")) { while (true) delay(10) }

        CoroutineTeardown.awaitChildren(job, logger, gracePeriodMs = 100, forceCancelTimeoutMs = 1000)

        assertEquals(false, messages.any { it.contains("Abandoning") })
        assertTrue(messages.any { it.contains("Cleaned up 1/1") }, messages.toString())
        job.cancel()
    }
}
