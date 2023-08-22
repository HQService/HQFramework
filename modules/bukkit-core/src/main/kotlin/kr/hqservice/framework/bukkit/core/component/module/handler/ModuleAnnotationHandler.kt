package kr.hqservice.framework.bukkit.core.component.module.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation

@AnnotationHandler
class ModuleAnnotationHandler : HQAnnotationHandler<Module> {
    override fun setup(instance: Any, annotation: Module) {
        instance::class.declaredMemberFunctions
            .filter { it.hasAnnotation<Setup>() }
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

    override fun teardown(instance: Any, annotation: Module) {
        instance::class.declaredMemberFunctions
            .filter { it.hasAnnotation<Teardown>() }
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