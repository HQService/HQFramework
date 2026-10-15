package kr.hqservice.framework.proxy.core.component.module.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions

abstract class ProxyModuleAnnotationHandler<M : Annotation>(
    private val setupAnnotation: KClass<out Annotation>,
    private val teardownAnnotation: KClass<out Annotation>
) : HQAnnotationHandler<M> {
    override fun setup(instance: Any, annotation: M) {
        invokeAnnotated(instance, setupAnnotation)
    }

    override fun teardown(instance: Any, annotation: M) {
        invokeAnnotated(instance, teardownAnnotation)
    }

    private fun invokeAnnotated(instance: Any, marker: KClass<out Annotation>) {
        instance::class.declaredMemberFunctions
            .filter { function -> function.annotations.any { marker.isInstance(it) } }
            .forEach {
                if (it.isSuspend) {
                    runBlocking {
                        it.callSuspend(instance)
                    }
                } else {
                    it.call(instance)
                }
            }
    }
}
