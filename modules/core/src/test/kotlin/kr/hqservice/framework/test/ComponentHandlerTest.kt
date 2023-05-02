package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import kr.hqservice.framework.core.HQFrameworkModule
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQModule
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.core.component.error.NoBeanDefinitionsFoundException
import kr.hqservice.framework.core.component.registry.impl.ComponentRegistryImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.module
import java.util.logging.Logger
import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ComponentHandlerTest : KoinComponent {
    @MockK
    private lateinit var plugin: HQPlugin

    @BeforeEach
    fun setup() {
        MockBukkit.mock()
        startKoin {
            modules(HQFrameworkModule().module)
        }
        every { plugin.logger } returns Logger.getLogger("TEST")
        every { plugin.config } returns mockk()
    }

    interface TestHQModule : HQModule {
        override fun onEnable(plugin: HQPlugin) {
            println("onEnable, ${this::class.simpleName}")
            assert(true)
        }

        override fun onDisable(plugin: HQPlugin) {
            println("onDisable, ${this::class.simpleName}")
            assert(true)
        }
    }

    @Component
    class TestComponentA : TestHQModule

    @Component
    class TestComponentB : TestHQModule

    @HQSingleton(binds = [TestComponentC::class])
    @Component
    class TestComponentC : TestHQModule

    @Component
    class TestComponentD(testComponentC: TestComponentC) : TestHQModule

    @Component
    class TestComponentE(dummy: Dummy) : TestHQModule

    @Component
    class TestComponentF(componentG: TestComponentG) : TestHQModule

    @HQSingleton(binds = [TestComponentG::class])
    @Component
    class TestComponentG : TestHQModule

    class Dummy

    @Test
    fun component_handler_test() {
        val mock = spyk(ComponentRegistryImpl(plugin), recordPrivateCalls = true)
        every { mock["getAllPluginClasses"]() } returns listOf<KClass<*>>(
            TestComponentA::class,
            TestComponentB::class,
            TestComponentC::class,
            TestComponentD::class,
            TestComponentE::class,
            TestComponentF::class,
            TestComponentG::class
        )
        try {
            mock.setup()
        } catch (exception: NoBeanDefinitionsFoundException) {
            assert(exception.classes.size == 1)
        }
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
        stopKoin()
    }
}