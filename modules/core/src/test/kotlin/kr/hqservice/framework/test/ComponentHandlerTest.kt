@file:Suppress("DEPRECATION")

package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kr.hqservice.framework.core.HQFrameworkModule
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.*
import kr.hqservice.framework.core.component.error.NoBeanDefinitionsFoundException
import kr.hqservice.framework.core.component.registry.ComponentRegistry
import kr.hqservice.framework.core.component.registry.impl.ComponentRegistryImpl
import kr.hqservice.framework.core.extension.print
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
import org.koin.ksp.generated.module
import java.util.logging.Logger

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ComponentHandlerTest : KoinComponent {
    @MockK
    private lateinit var plugin: HQPlugin
    private lateinit var componentRegistry: ComponentRegistry
    private val capturedListener = slot<Listener>()

    @Suppress("DEPRECATION", "removal")
    @BeforeEach
    fun setup() {
        val server = MockBukkit.mock()
        startKoin {
            modules(HQFrameworkModule().module)
        }
        every { plugin.logger } returns Logger.getLogger("TEST")
        every { plugin.config } returns mockk()
        every { plugin.server } returns server
        every { plugin.isEnabled } returns true
        every { plugin.name } returns "HQTestPlugin"
        val pluginLoader = mockk<PluginLoader>()
        every { plugin.pluginLoader } returns pluginLoader

        every { pluginLoader.createRegisteredListeners(capture(capturedListener), any()) } returns mapOf()

        componentRegistry = spyk(ComponentRegistryImpl(plugin), recordPrivateCalls = true)
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
        stopKoin()
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

    @HQSingleton
    @Component
    class TestComponentA : TestHQModule, HQListener

    @Component
    class TestComponentB : TestHQModule

    @HQFactory(binds = [TestComponentC::class])
    @Component
    class TestComponentC : TestHQModule

    @Component
    class TestComponentD(testComponentC: TestComponentC) : TestHQModule, KoinComponent {
        private val testComponentF: TestComponentF by inject()

        override fun onEnable() {
            super.onEnable()
            testComponentF.toString().print("testComponentF: ")
        }
    }

    @Component
    class TestComponentE(componentG: TestComponentF) : TestHQModule

    @HQSingleton(binds = [TestComponentF::class])
    @Component
    class TestComponentF : TestHQModule

    @Component
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
        } catch (exception: NoBeanDefinitionsFoundException) {
            assert(exception.classes.size == 1)
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
        val testComponentA: TestComponentA by inject()
        assert(capturedListener.captured == testComponentA)
    }

    private fun setAllPluginClasses(vararg classes: Class<*>) {
        every { componentRegistry["getAllPluginClasses"]() } returns classes.toList()
    }
}