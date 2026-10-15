package kr.hqservice.framework.bukkit.core.component.module.handler

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation

@AnnotationHandler
class ModuleAnnotationHandler(private val plugin: HQBukkitPlugin) : HQAnnotationHandler<Module> {
    private val lifecycleContext: CoroutineContext
        get() = plugin.coroutineContext.minusKey(Job).minusKey(ContinuationInterceptor)

    override fun setup(instance: Any, annotation: Module) {
        instance::class.declaredMemberFunctions
            .filter { it.hasAnnotation<Setup>() }
            .forEach {
                if (it.isSuspend) {
                    runBlocking(lifecycleContext) {
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
                    runBlocking(lifecycleContext) {
                        it.callSuspend(instance)
                    }
                } else {
                    it.call(instance)
                }
            }
    }
}