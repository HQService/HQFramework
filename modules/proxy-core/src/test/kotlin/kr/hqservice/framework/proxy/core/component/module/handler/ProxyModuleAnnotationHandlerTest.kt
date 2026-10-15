package kr.hqservice.framework.proxy.core.component.module.handler

import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure
import kotlin.test.Test
import kotlin.test.assertEquals

class ProxyModuleAnnotationHandlerTest {
    annotation class TestModule
    annotation class TestSetup
    annotation class TestTeardown

    class TestHandler : ProxyModuleAnnotationHandler<TestModule>(TestSetup::class, TestTeardown::class)

    @TestModule
    class SampleModule {
        val calls = mutableListOf<String>()

        @TestSetup
        fun setup() {
            calls.add("setup")
        }

        @TestSetup
        suspend fun suspendSetup() {
            calls.add("suspendSetup")
        }

        @TestTeardown
        fun teardown() {
            calls.add("teardown")
        }

        fun untouched() {
            calls.add("untouched")
        }
    }

    @Test
    fun `annotation type argument resolves to the platform module annotation`() {
        val argument = TestHandler::class.supertypes
            .first { it.isSubtypeOf(HQAnnotationHandler::class.starProjectedType) }
            .arguments.first().type!!.jvmErasure

        assertEquals(TestModule::class, argument)
    }

    @Test
    fun `setup and teardown invoke only their annotated functions`() {
        val module = SampleModule()
        val handler = TestHandler()
        val annotation = SampleModule::class.annotations.filterIsInstance<TestModule>().first()

        handler.setup(module, annotation)
        assertEquals(setOf("setup", "suspendSetup"), module.calls.toSet())

        module.calls.clear()
        handler.teardown(module, annotation)
        assertEquals(listOf("teardown"), module.calls)
    }
}
