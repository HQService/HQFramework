package kr.hqservice.framework.bukkit.core.coroutine.dispatcher

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kr.hqservice.framework.bukkit.core.TestHQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import org.bukkit.Server
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BukkitDispatcherTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TestHQBukkitPlugin

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
    fun `main dispatch from the main thread runs inline`() {
        var ran = false
        plugin.launch { ran = true }
        assertTrue(ran)
    }
}
