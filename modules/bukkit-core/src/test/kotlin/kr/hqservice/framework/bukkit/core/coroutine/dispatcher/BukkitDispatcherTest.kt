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
import kotlinx.coroutines.withTimeout
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

    @Test
    fun `main dispatch from the main thread runs inline while the plugin is enabling`() {
        val enablingPlugin = mockk<HQBukkitPlugin>()
        every { enablingPlugin.isEnabling } returns true
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(enablingPlugin))
        var ran = false

        scope.launch { ran = true }

        assertTrue(ran)
    }

    @Test
    fun `main dispatch from the main thread waits for the next tick once the plugin is enabled`() {
        val runnable = slot<() -> Unit>()
        val scheduler = mockk<HQScheduler>()
        every { scheduler.runTask(capture(runnable)) } just Runs
        val enabledPlugin = mockk<HQBukkitPlugin>()
        every { enabledPlugin.isEnabling } returns false
        every { enabledPlugin.getScheduler() } returns scheduler
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + PluginCoroutineContextElement(enabledPlugin))
        var ran = false

        scope.launch { ran = true }

        assertFalse(ran)
        runnable.captured.invoke()
        assertTrue(ran)
    }
}
