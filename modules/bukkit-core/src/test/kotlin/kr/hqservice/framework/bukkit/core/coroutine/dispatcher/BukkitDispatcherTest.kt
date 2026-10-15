package kr.hqservice.framework.bukkit.core.coroutine.dispatcher

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kr.hqservice.framework.bukkit.core.coroutine.LifecycleMainThread
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import org.bukkit.Server
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BukkitDispatcherTest {
    private lateinit var server: ServerMock

    private fun lifecyclePlugin(enabling: Boolean, disabling: Boolean): HQBukkitPlugin {
        val plugin = mockk<HQBukkitPlugin>()
        setLifecycleField(plugin, "isEnabling", enabling)
        setLifecycleField(plugin, "isDisabling", disabling)
        every { plugin.isLifecycleBlockingMainThread } answers { callOriginal() }
        return plugin
    }

    private fun setLifecycleField(plugin: HQBukkitPlugin, name: String, value: Boolean) {
        HQBukkitPlugin::class.java.getDeclaredField(name).apply { isAccessible = true }.setBoolean(plugin, value)
    }

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun `delays shorter than a tick are clamped to one tick`() {
        assertEquals(1L, ticksFor(1))
        assertEquals(1L, ticksFor(49))
        assertEquals(1L, ticksFor(50))
        assertEquals(2L, ticksFor(100))
    }

    @Test
    fun `coroutines dispatched after the plugin is disabled still complete`() {
        val bukkitScheduler = mockk<BukkitScheduler>()
        every { bukkitScheduler.runTask(any<Plugin>(), any<Runnable>()) } throws IllegalPluginAccessException("disabled")
        val disabledServer = mockk<Server>()
        every { disabledServer.scheduler } returns bukkitScheduler
        val disabledPlugin = mockk<Plugin>()
        every { disabledPlugin.server } returns disabledServer
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(disabledPlugin))

        val job = runBlocking(Dispatchers.IO) {
            val job = scope.launch { }
            withTimeout(1000) { job.join() }
            job
        }
        assertTrue(job.isCancelled)
    }

    private fun pluginWithoutScheduler(): HQBukkitPlugin {
        val plugin = lifecyclePlugin(enabling = false, disabling = false)
        every { plugin.getScheduler() } throws AssertionError("the bukkit scheduler must not be used while the main thread is blocked")
        return plugin
    }

    @Test
    fun `main dispatch from the main thread runs inline while a lifecycle runBlocking holds the main thread`() {
        val plugin = pluginWithoutScheduler()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(plugin))
        var ran = false

        LifecycleMainThread.runBlockingOnMainThread { scope.launch { ran = true } }

        assertTrue(ran)
    }

    @Test
    fun `a resume from another thread while the main thread is blocked runs on the blocking loop`() {
        val plugin = pluginWithoutScheduler()
        var thread: Thread? = null

        LifecycleMainThread.runBlockingOnMainThread {
            withTimeout(2000) {
                withContext(Dispatchers.BukkitMain + PluginCoroutineContextElement(plugin)) {
                    withContext(Dispatchers.IO) { Thread.sleep(20) }
                    thread = Thread.currentThread()
                }
            }
        }

        assertEquals(Thread.currentThread(), thread)
    }

    @Test
    fun `delay on the main dispatcher completes while the main thread is blocked`() {
        val plugin = pluginWithoutScheduler()
        var elapsed = 0L

        LifecycleMainThread.runBlockingOnMainThread {
            withTimeout(2000) {
                withContext(Dispatchers.BukkitMain + PluginCoroutineContextElement(plugin)) {
                    val started = System.nanoTime()
                    delay(60)
                    elapsed = (System.nanoTime() - started) / 1_000_000
                }
            }
        }

        assertTrue(elapsed >= 50, "delay returned after ${elapsed}ms")
    }

    @Test
    fun `main thread work queued before disable is drained instead of dropped`() {
        val runnables = mutableListOf<() -> Unit>()
        val scheduler = mockk<HQScheduler>()
        every { scheduler.runTask(capture(runnables)) } just Runs
        every { scheduler.runTaskLater(any(), any<() -> Unit>()) } returns mockk<kr.hqservice.framework.bukkit.core.scheduler.HQTask>(relaxed = true)
        val plugin = lifecyclePlugin(enabling = false, disabling = false)
        every { plugin.getScheduler() } returns scheduler
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(plugin))
        var ran = false
        var delayedFinally = false
        scope.launch { ran = true }
        scope.launch { try { delay(10_000) } finally { delayedFinally = true } }
        runnables[1].invoke()
        assertFalse(ran)
        assertFalse(delayedFinally)

        LifecycleMainThread.runBlockingOnMainThread {
            BukkitDispatcher.drainPending(plugin)
            delay(50)
        }

        assertTrue(ran)
        assertTrue(delayedFinally)
    }

    @Test
    fun `main dispatch from the main thread waits for the next tick once the plugin is enabled`() {
        val runnable = slot<() -> Unit>()
        val scheduler = mockk<HQScheduler>()
        every { scheduler.runTask(capture(runnable)) } just Runs
        val enabledPlugin = lifecyclePlugin(enabling = false, disabling = false)
        every { enabledPlugin.getScheduler() } returns scheduler
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(enabledPlugin))
        var ran = false

        scope.launch { ran = true }

        assertFalse(ran)
        runnable.captured.invoke()
        assertTrue(ran)
    }

    @Test
    fun `coroutines of another plugin still wait for the scheduler while one plugin blocks the main thread`() {
        val blocking = pluginWithoutScheduler()
        val other = lifecyclePlugin(enabling = false, disabling = false)
        val runnables = mutableListOf<() -> Unit>()
        val scheduler = mockk<HQScheduler>()
        every { scheduler.runTask(capture(runnables)) } just Runs
        every { other.getScheduler() } returns scheduler
        val otherScope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(other))
        var ran = false

        LifecycleMainThread.runBlockingOnMainThread(PluginCoroutineContextElement(blocking)) {
            otherScope.launch { ran = true }
        }

        assertFalse(ran)
        assertEquals(1, runnables.size)
        runnables.single().invoke()
        assertTrue(ran)
    }

    @Test
    fun `a delay that outlives the lifecycle block resumes through the scheduler instead of a background thread`() {
        val plugin = lifecyclePlugin(enabling = false, disabling = false)
        val runnables = mutableListOf<() -> Unit>()
        val scheduler = mockk<HQScheduler>()
        every { scheduler.runTask(capture(runnables)) } just Runs
        every { plugin.getScheduler() } returns scheduler
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(plugin))
        var resumedOn: Thread? = null

        LifecycleMainThread.runBlockingOnMainThread(PluginCoroutineContextElement(plugin)) {
            scope.launch { delay(150); resumedOn = Thread.currentThread() }
        }
        Thread.sleep(400)

        assertEquals(null, resumedOn, "must not resume on a background thread")
        assertEquals(1, runnables.size)
        runnables.single().invoke()
        assertEquals(Thread.currentThread(), resumedOn)
    }
}
