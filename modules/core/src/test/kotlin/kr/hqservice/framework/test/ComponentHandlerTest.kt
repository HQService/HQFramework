package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kr.hqservice.framework.core.HQFrameworkModule
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.handler.ComponentHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.module

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
    }

    @Test
    fun component_handler_test() {
        val componentHandlers = getKoin().getAll<ComponentHandler<*>>()
        componentHandlers.forEach { componentHandler ->
            val className = componentHandler::class.qualifiedName
            println("className: $className")
        }
    }
    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
        stopKoin()
    }
}