package kr.hqservice.framework.global.core.component.registry

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.Primary
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.error.ComponentCircularException
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ListInjectionTest {
    private class TestRegistry(
        private val classes: List<Class<*>>,
        private val parent: AbstractComponentRegistry? = null
    ) : AbstractComponentRegistry() {
        override fun getAllComponentsToScan(): Collection<Class<*>> = classes

        override fun findParentRegistry(): AbstractComponentRegistry? = parent

        override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> = mutableMapOf()

        override fun getConfiguration(): HQYamlConfiguration = throw UnsupportedOperationException()
    }

    interface Rule

    @Component
    class RuleA : Rule

    @Component
    class RuleB : Rule

    @Component
    @Primary
    class PrimaryRule : Rule

    @Component
    class Consumer(val rules: List<Rule>)

    @Component
    class CollectionConsumer(val rules: Collection<Rule>)

    @Bean
    class LazyRule : Rule

    @Configuration
    class RuleConfiguration {
        @Bean
        fun configuredRule(): Rule = ConfiguredRule()
    }

    class ConfiguredRule : Rule

    @Configuration
    class QualifiedRuleConfiguration {
        @Bean
        @Qualifier("fast")
        fun fast(): Rule = ConfiguredRule()

        @Bean
        @Qualifier("fast")
        fun alsoFast(): Rule = ConfiguredRule()

        @Bean
        @Qualifier("slow")
        fun slow(): Rule = ConfiguredRule()
    }

    @Component
    class QualifiedConsumer(@Qualifier("fast") val rules: List<Rule>)

    @Component
    class CyclicRule(val consumer: Consumer) : Rule

    class RuntimeConsumer(val rules: List<Rule>)

    @BeforeEach
    fun setup() {
        startKoin { }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    private fun koin() = GlobalContext.get()

    @Test
    fun `consumer scanned before the implementations receives all of them`() {
        TestRegistry(listOf(Consumer::class.java, RuleA::class.java, RuleB::class.java)).setup()

        assertEquals(setOf(RuleA::class, RuleB::class), koin().get<Consumer>().rules.map { it::class }.toSet())
    }

    @Test
    fun `collection parameter is supported too`() {
        TestRegistry(listOf(RuleA::class.java, CollectionConsumer::class.java)).setup()

        assertEquals(listOf(RuleA::class), koin().get<CollectionConsumer>().rules.map { it::class })
    }

    @Test
    fun `injected instances are the registered singletons`() {
        TestRegistry(listOf(RuleA::class.java, Consumer::class.java)).setup()

        assertSame(koin().get<RuleA>(), koin().get<Consumer>().rules.single())
    }

    @Test
    fun `primary comes first whatever the scan order`() {
        TestRegistry(listOf(RuleA::class.java, PrimaryRule::class.java, RuleB::class.java, Consumer::class.java)).setup()

        val rules = koin().get<Consumer>().rules
        assertInstanceOf(PrimaryRule::class.java, rules.first())
        assertEquals(listOf(PrimaryRule::class, RuleA::class, RuleB::class), rules.map { it::class })
    }

    @Test
    fun `plain bean registered after a primary is still part of the list`() {
        TestRegistry(listOf(PrimaryRule::class.java, RuleA::class.java, Consumer::class.java)).setup()

        assertEquals(listOf(PrimaryRule::class, RuleA::class), koin().get<Consumer>().rules.map { it::class })
    }

    @Test
    fun `lazy beans and configuration beans are included`() {
        TestRegistry(listOf(Consumer::class.java, LazyRule::class.java, RuleConfiguration::class.java)).setup()

        assertEquals(setOf(LazyRule::class, ConfiguredRule::class), koin().get<Consumer>().rules.map { it::class }.toSet())
    }

    @Test
    fun `qualifier on the list parameter filters the candidates`() {
        TestRegistry(listOf(QualifiedConsumer::class.java, QualifiedRuleConfiguration::class.java, RuleA::class.java)).setup()

        assertEquals(2, koin().get<QualifiedConsumer>().rules.size)
    }

    @Test
    fun `no implementation yields an empty list`() {
        TestRegistry(listOf(Consumer::class.java)).setup()

        assertEquals(emptyList<Rule>(), koin().get<Consumer>().rules)
    }

    @Test
    fun `implementation depending on the list consumer is reported as a cycle`() {
        assertThrows(ComponentCircularException::class.java) {
            TestRegistry(listOf(Consumer::class.java, CyclicRule::class.java)).setup()
        }
    }

    @Test
    fun `list sees the parent registry but not sibling registries`() {
        val parent = TestRegistry(listOf(RuleA::class.java))
        parent.setup()
        TestRegistry(listOf(RuleB::class.java), parent).setup()
        TestRegistry(listOf(Consumer::class.java), parent).setup()

        assertEquals(listOf(RuleA::class), koin().get<Consumer>().rules.map { it::class })
    }

    @Test
    fun `runtime injection after setup resolves the full list`() {
        val registry = TestRegistry(listOf(RuleA::class.java, RuleB::class.java))
        registry.setup()

        val injected = registry.injectParameters(RuntimeConsumer::class.primaryConstructor!!, null)

        assertEquals(setOf(RuleA::class, RuleB::class), (injected.single() as List<*>).map { it!!::class }.toSet())
    }

    @Test
    fun `teardown forgets the candidates`() {
        val registry = TestRegistry(listOf(RuleA::class.java))
        registry.setup()
        registry.teardown()

        val injected = registry.injectParameters(RuntimeConsumer::class.primaryConstructor!!, null)

        assertEquals(emptyList<Rule>(), injected.single())
    }
}
