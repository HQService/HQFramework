package kr.hqservice.framework.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * 이 테스트는, Koin 에서 Singleton 으로 선언한 Bean 의 경우에는
 * 넘겨주는 Parameter 가 다른경우에도 처음 생성된 객체를 반환하는 것을 보여줍니다.
 */
class ParameterizedBeanDefinitionTest : KoinComponent {
    interface ExampleClass {
        val key: String
    }

    class ExampleClassImpl(@InjectedParam override val key: String) : ExampleClass

    @BeforeEach
    fun setup() {
        startKoin {
            val module = module {
                single<ExampleClass> { ExampleClassImpl(get()) }
            }
            modules(module)
        }
    }

    @Test
    fun same_object_will_be_inject_test() {
        val injected1: ExampleClass by inject { parametersOf("id1") }
        val injected2: ExampleClass by inject { parametersOf("id2") }
        assert(injected1.key == "id1")
        assert(injected2.key != "id2")
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }
}