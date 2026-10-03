package kr.hqservice.framework.bukkit.core

import be.seeseemelk.mockbukkit.MockBukkit
import kotlinx.coroutines.Job
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

class PluginScopeCompletionTest {
    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `disabling the plugin completes its coroutine scope`() {
        val server = MockBukkit.getMock()!!
        val plugin = TestHQBukkitPlugin.load(server)
        val completed = AtomicBoolean(false)
        plugin.coroutineContext[Job]!!.invokeOnCompletion { completed.set(true) }

        server.pluginManager.disablePlugin(plugin)

        assertTrue(completed.get())
        assertTrue(plugin.coroutineContext[Job]!!.isCancelled)
    }

    @Test
    fun `an HQCoroutineScope component is a child of the plugin scope`() {
        val server = MockBukkit.getMock()!!
        val plugin = TestHQBukkitPlugin.load(server)
        val scope = object : HQCoroutineScope(plugin, kotlinx.coroutines.Dispatchers.Default) {}

        server.pluginManager.disablePlugin(plugin)

        assertTrue(scope.getSupervisor().isCancelled)
    }

    @Test
    fun `re-enabling the same plugin instance gives it a live scope again`() {
        val server = MockBukkit.getMock()!!
        val plugin = TestHQBukkitPlugin.load(server)
        server.pluginManager.disablePlugin(plugin)
        assertTrue(plugin.coroutineContext[Job]!!.isCancelled)

        server.pluginManager.enablePlugin(plugin)

        assertTrue(plugin.coroutineContext[Job]!!.isActive)
        server.pluginManager.disablePlugin(plugin)
    }
}
