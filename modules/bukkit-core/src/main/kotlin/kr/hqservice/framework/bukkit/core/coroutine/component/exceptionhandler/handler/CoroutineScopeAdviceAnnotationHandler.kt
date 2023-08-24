package kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.handler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.*
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kr.hqservice.framework.global.core.extension.print
import java.lang.Exception
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@AnnotationHandler
class CoroutineScopeAdviceAnnotationHandler : HQAnnotationHandler<CoroutineScopeAdvice> {
    override fun setup(instance: Any, annotation: CoroutineScopeAdvice) {
        instance::class.memberFunctions
            .filterIsInstance<KFunction<Unit>>()
            .filter { it.hasAnnotation<ExceptionHandler>() }
            .forEach { function ->
                val exceptionHandler = function.findAnnotation<ExceptionHandler>()!!
                val bakedHandler = bakeAttachableExceptionHandler(exceptionHandler, function, instance)
                when (annotation.type) {
                    AdviceType.GLOBAL -> HQBukkitPlugin.GlobalExceptionHandlerRegistry.attachExceptionHandler(bakedHandler)
                    AdviceType.PLUGIN -> {
                        val plugin = PluginScopeFinder.get(instance::class)
                        plugin.attachExceptionHandler(bakedHandler)
                    }
                }
            }
    }

    private fun bakeAttachableExceptionHandler(exceptionHandler: ExceptionHandler, function: KFunction<Unit>, obj: Any): AttachableExceptionHandler {
        return object : AttachableExceptionHandler {
            override val priority: Int
                get() = exceptionHandler.priority

            override fun handle(throwable: Throwable): HandleResult {
                val exceptionClass = function.valueParameters.firstOrNull {
                    it.type.jvmErasure.isSubclassOf(Exception::class) || it.type.jvmErasure.isSubclassOf(Throwable::class)
                }
                    ?: throw IllegalStateException("ExceptionHandler 의 value parameter 에는 Exception 이 들어와야합니다. function name: ${function.name}, value parameters: ${function.valueParameters}")
                if (throwable::class.starProjectedType == exceptionClass.type) {
                    function.call(obj, throwable)
                    if (function.hasAnnotation<MustBeStored>()) {
                        return HandleResult.MUST_STORE
                    } else {
                        return HandleResult.HANDLED
                    }
                }
                return HandleResult.UNHANDLED
            }
        }
    }
}