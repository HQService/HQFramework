package kr.hqservice.framework.global.core.component.registry

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.HQComponent
import kr.hqservice.framework.global.core.component.Primary
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import kotlin.reflect.KClass

class ComponentRegistryQueueTest {
    private class TestRegistry(
        private val classes: List<Class<*>>,
        private val parent: AbstractComponentRegistry? = null
    ) : AbstractComponentRegistry() {
        override fun getAllComponentsToScan(): Collection<Class<*>> = classes

        override fun findParentRegistry(): AbstractComponentRegistry? = parent

        override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> = mutableMapOf()

        override fun getConfiguration(): HQYamlConfiguration = throw UnsupportedOperationException()
    }

    @Component
    class NeedsZ(val z: NeedsS)

    @Component
    class NeedsS(val s: ServiceS)

    @Service
    class ServiceS

    class ConfiguredValue

    @Configuration
    class CountingConfiguration {
        init {
            instantiations++
        }

        @Bean
        fun configuredValue(): ConfiguredValue = ConfiguredValue()
    }

    class Named(val name: String)

    @Configuration
    class QualifiedConfiguration {
        @Bean
        @Qualifier("a")
        fun a(): Named = Named("a")

        @Bean
        @Qualifier("b")
        fun b(): Named = Named("b")
    }

    interface Greeter

    @Component
    @Primary
    class PrimaryGreeter : Greeter

    @Component
    class PlainGreeter : Greeter

    class Missing

    @Component
    class Optional(val missing: Missing?)

    @Component
    class LateDependency

    @Component
    class OptionalDependent(val dependency: LateDependency?)

    interface Handled : HQComponent

    @Component
    class HandledComponent : Handled

    @ComponentHandler
    class HandledComponentHandler : HQComponentHandler<Handled> {
        override fun setup(element: Handled) {
            handledCount++
        }
    }

    @Component
    class Standalone

    companion object {
        var instantiations = 0
        var handledCount = 0
    }

    @BeforeEach
    fun setup() {
        instantiations = 0
        handledCount = 0
        startKoin { }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    private fun koin() = GlobalContext.get()

    @Test
    fun `queue progress resets after a bean class registers`() {
        TestRegistry(listOf(NeedsZ::class.java, NeedsS::class.java, ServiceS::class.java)).setup()

        assertNotNull(koin().getOrNull<NeedsZ>())
    }

    @Test
    fun `configuration class is instantiated once`() {
        TestRegistry(listOf(CountingConfiguration::class.java)).setup()

        assertEquals(1, instantiations)
        assertNotNull(koin().getOrNull<ConfiguredValue>())
        assertNotNull(koin().getOrNull<CountingConfiguration>())
        assertEquals(1, instantiations)
    }

    @Test
    fun `bean functions of the same type with distinct qualifiers are all registered`() {
        TestRegistry(listOf(QualifiedConfiguration::class.java)).setup()

        assertEquals("a", koin().get<Named>(named("a")).name)
        assertEquals("b", koin().get<Named>(named("b")).name)
    }

    @Test
    fun `primary wins when registered after a plain bean`() {
        TestRegistry(listOf(PlainGreeter::class.java, PrimaryGreeter::class.java)).setup()

        assertInstanceOf(PrimaryGreeter::class.java, koin().get<Greeter>())
    }

    @Test
    fun `primary wins when registered before a plain bean`() {
        TestRegistry(listOf(PrimaryGreeter::class.java, PlainGreeter::class.java)).setup()

        assertInstanceOf(PrimaryGreeter::class.java, koin().get<Greeter>())
    }

    @Test
    fun `unresolvable nullable parameter resolves to null`() {
        TestRegistry(listOf(Optional::class.java)).setup()

        assertNull(koin().get<Optional>().missing)
    }

    @Test
    fun `nullable parameter waits for a dependency scanned later`() {
        TestRegistry(listOf(OptionalDependent::class.java, LateDependency::class.java)).setup()

        assertNotNull(koin().get<OptionalDependent>().dependency)
    }

    @Test
    fun `teardown unloads the definitions the registry loaded`() {
        val registry = TestRegistry(listOf(Standalone::class.java))
        registry.setup()
        assertNotNull(koin().getOrNull<Standalone>())

        registry.teardown()

        assertNull(koin().getOrNull<Standalone>())
    }

    @Test
    fun `handlers of one registry do not apply to another registry`() {
        TestRegistry(listOf(HandledComponentHandler::class.java)).setup()
        TestRegistry(listOf(HandledComponent::class.java)).setup()

        assertEquals(0, handledCount)
    }

    @Test
    fun `child registry applies handlers inherited from its parent`() {
        val parent = TestRegistry(listOf(HandledComponentHandler::class.java))
        parent.setup()
        TestRegistry(listOf(HandledComponent::class.java), parent).setup()

        assertEquals(1, handledCount)
    }
}
