package kr.hqservice.framework.bukkit.core.listener

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kr.hqservice.framework.bukkit.core.TestHQBukkitPlugin
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SuspendEventExecutorTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TestHQBukkitPlugin

    class TestCancellableEvent : Event(), Cancellable {
        private var cancelled = false
        override fun isCancelled(): Boolean = cancelled
        override fun setCancelled(cancel: Boolean) {
            cancelled = cancel
        }
        override fun getHandlers(): HandlerList = handlerList

        companion object {
            @JvmStatic
            val handlerList = HandlerList()
        }
    }

    class CancellingListener {
        val resumedOnPrimaryThread = CompletableDeferred<Boolean>()

        suspend fun handle(event: Cancellable) {
            event.isCancelled = true
            delay(1)
            resumedOnPrimaryThread.complete(Bukkit.isPrimaryThread())
        }
    }

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        plugin = TestHQBukkitPlugin.load(server)
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun `suspend listener cancels the event before execute returns and resumes on the main thread`() {
        val listener = CancellingListener()
        val executor = SuspendEventExecutor(TestCancellableEvent::class, listener, CancellingListener::handle, plugin)
        val event = TestCancellableEvent()

        executor.execute(object : Listener {}, event)

        assertTrue(event.isCancelled)
        server.scheduler.performTicks(2)
        val resumedOnPrimaryThread = runBlocking { withTimeout(1000) { listener.resumedOnPrimaryThread.await() } }
        assertTrue(resumedOnPrimaryThread)
    }
}
