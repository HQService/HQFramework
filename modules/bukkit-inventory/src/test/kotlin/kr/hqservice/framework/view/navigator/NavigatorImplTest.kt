package kr.hqservice.framework.view.navigator

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kotlinx.coroutines.*
import kr.hqservice.framework.inventory.test.addPlayerWithCraftingView
import kr.hqservice.framework.inventory.test.resetToCraftingView
import kr.hqservice.framework.inventory.test.topHolder
import kr.hqservice.framework.view.navigator.impl.NavigatorImpl
import org.bukkit.entity.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@Timeout(value = 20, unit = TimeUnit.SECONDS)
class NavigatorImplTest {
    private lateinit var server: ServerMock
    private lateinit var player: Player
    private lateinit var scope: CoroutineScope
    private lateinit var navigator: NavigatorImpl

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        MockBukkit.createMockPlugin("HQFramework")
        player = server.addPlayerWithCraftingView("ABC")
        scope = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, _ -> })
        navigator = NavigatorImpl(scope)
    }

    @AfterEach
    fun tearDown() {
        scope.cancel()
        MockBukkit.unmock()
    }

    @Test
    fun currentReturnsNullWhenStackIsEmpty() {
        val view = RecordingView()
        openAndAwait(view)
        runOffMain { navigator.goPrevious(player) }

        assertNull(navigator.current(player.uniqueId))
    }

    @Test
    fun goPreviousOnSingleViewEmptiesStackAndDisposesView() {
        val view = RecordingView()
        openAndAwait(view)

        runOffMain { navigator.goPrevious(player) }

        assertTrue(navigator.openedViews(player.uniqueId).isEmpty())
        assertTrue(view.isDisposed)
    }

    @Test
    fun goNextWorksAfterPreviousOpenFailedInOnCreate() {
        openAndAwait(RecordingView())
        val failing = RecordingView(failOnCreate = true)
        runOffMain { navigator.goNext(failing, player) }
        tickUntil { failing.createCount.get() == 1 && !navigator.isAllowToChangeView(player.uniqueId) }

        server.resetToCraftingView(player)
        val next = RecordingView()
        openAndAwait(next)

        assertSame(next, navigator.current(player.uniqueId))
    }

    @Test
    fun reopeningViewThroughBackNavigationDoesNotRunOnCreateAgain() {
        val first = RecordingView()
        openAndAwait(first)
        openAndAwait(RecordingView())

        runOffMain { navigator.goPrevious(player) }
        tickUntil { player.topHolder === first && !navigator.isAllowToChangeView(player.uniqueId) }

        assertEquals(1, first.createCount.get())
    }

    @Test
    fun clearRemovesStackClosesCurrentViewAndDisposesAllViews() {
        val first = RecordingView()
        val second = RecordingView()
        openAndAwait(first)
        openAndAwait(second)

        navigator.clear(player)

        assertTrue(navigator.openedViews(player.uniqueId).isEmpty())
        assertEquals(listOf(player.uniqueId), second.closedViewers)
        assertTrue(first.closedViewers.isEmpty())
        assertTrue(first.isDisposed)
        assertTrue(second.isDisposed)
    }

    private fun openAndAwait(view: RecordingView) {
        runOffMain { navigator.goNext(view, player) }
        tickUntil { player.topHolder === view && !navigator.isAllowToChangeView(player.uniqueId) }
    }

    private fun runOffMain(block: suspend () -> Unit) {
        val job = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, _ -> }).launch { block() }
        tickUntil { job.isCompleted }
    }

    private fun tickUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "condition was not met in time" }
            server.scheduler.performOneTick()
            Thread.sleep(5)
        }
    }
}
