package kr.hqservice.framework.bukkit.core.component.module

import be.seeseemelk.mockbukkit.MockBukkit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kr.hqservice.framework.bukkit.core.TestHQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.module.handler.ModuleAnnotationHandler
import kr.hqservice.framework.bukkit.core.coroutine.LifecycleMainThread
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.reflect.full.findAnnotation

class ModuleAnnotationHandlerTest {
    @Module
    class MainThreadModule {
        val steps = mutableListOf<String>()

        @Setup
        suspend fun setup() {
            withContext(Dispatchers.BukkitMain) {
                withContext(Dispatchers.IO) { steps += "io" }
                delay(30)
                steps += "main"
            }
        }

        @Teardown
        suspend fun teardown() {
            delay(10)
            steps += "teardown"
        }
    }

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `suspend setup that hops to the main thread completes while the main thread is blocked by enable`() {
        val plugin = TestHQBukkitPlugin.load(MockBukkit.getMock()!!)
        val module = MainThreadModule()
        val handler = ModuleAnnotationHandler(plugin)
        val annotation = MainThreadModule::class.findAnnotation<Module>()!!

        assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            LifecycleMainThread.runBlockingOnMainThread {
                handler.setup(module, annotation)
                handler.teardown(module, annotation)
            }
        }

        assertEquals(listOf("io", "main", "teardown"), module.steps)
    }
}
