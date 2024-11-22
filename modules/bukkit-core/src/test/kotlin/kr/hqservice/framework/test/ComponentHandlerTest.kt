@file:Suppress("DEPRECATION")

package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Factory
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.global.core.component.error.NoBeanDefinitionsFoundException
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.global.core.extension.print
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginLoader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.*
import java.util.logging.Logger

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ComponentHandlerTest : KoinComponent {
    @MockK
    private lateinit var plugin: HQBukkitPlugin
    private lateinit var componentRegistry: ComponentRegistry
    private val capturedListener = slot<Listener>()

    @Suppress("DEPRECATION", "removal")
    @BeforeEach
    fun setup() {
        val server = MockBukkit.mock()
        every { plugin.logger } returns Logger.getLogger("TEST")
        every { plugin.config } returns mockk()
        every { plugin.server } returns server
        every { plugin.isEnabled } returns true
        every { plugin.name } returns "HQTestPlugin"
        val pluginLoader = mockk<PluginLoader>()
        every { plugin.pluginLoader } returns pluginLoader

        every { pluginLoader.createRegisteredListeners(capture(capturedListener), any()) } returns mapOf()

        componentRegistry = spyk(BukkitComponentRegistry(plugin), recordPrivateCalls = true)
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
        stopKoin()
        clearAllMocks()
    }

    interface TestHQModule : HQModule {
        override fun onEnable() {
            println("onEnable, ${this::class.simpleName}")
            assert(true)
        }

        override fun onDisable() {
            println("onDisable, ${this::class.simpleName}")
            assert(true)
        }
    }

    @Singleton
    @Component
    @ExcludeTestSearch
    class TestComponentA : TestHQModule

    @Component
    @ExcludeTestSearch
    class TestComponentB : TestHQModule

    @Factory(binds = [TestComponentC::class])
    @Component
    @ExcludeTestSearch
    class TestComponentC : TestHQModule

    @Component
    @ExcludeTestSearch
    class TestComponentD(testComponentC: TestComponentC) : TestHQModule, KoinComponent {
        private val testComponentF: TestComponentF by inject()

        override fun onEnable() {
            super.onEnable()
            testComponentF.toString().print("testComponentF: ")
        }
    }

    @Component
    @ExcludeTestSearch
    class TestComponentE(componentF: TestComponentF) : TestHQModule

    @Singleton(binds = [TestComponentF::class])
    @Component
    @ExcludeTestSearch
    class TestComponentF : TestHQModule

    @Component
    @ExcludeTestSearch
    class TestComponentX(dummy: Dummy) : TestHQModule
    class Dummy

    @Test
    fun component_handler_bean_not_found_exception_catch_test() {
        setAllPluginClasses(
            TestComponentA::class.java,
            TestComponentB::class.java,
            TestComponentC::class.java,
            TestComponentD::class.java,
            TestComponentX::class.java,
            TestComponentE::class.java,
            TestComponentF::class.java
        )
        try {
            componentRegistry.setup()
            assert(false)
        } catch (exception: NoBeanDefinitionsFoundException) {
            assert(true)
        }
    }

    @Test
    fun component_handler_duplicated_component_test() {
        setAllPluginClasses(
            TestComponentA::class.java,
            TestComponentB::class.java,
            TestComponentC::class.java,
            TestComponentD::class.java,
            TestComponentE::class.java,
            TestComponentF::class.java
        )
        componentRegistry.setup()
    }

    private fun setAllPluginClasses(vararg classes: Class<*>) {
        every { componentRegistry["getAllComponentsToScan"]() } returns listOf(
            *classes
        )
    }
}