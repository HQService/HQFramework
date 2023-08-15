package kr.hqservice.framework.bukkit.core.coroutine.component.handler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.component.*
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

@AnnotationHandler
class CoroutineScopeAdviceAnnotationHandler : HQAnnotationHandler<CoroutineScopeAdvice> {
    override fun setup(obj: Any, annotation: CoroutineScopeAdvice) {
        obj::class.memberFunctions
            .filterIsInstance<KFunction<Unit>>()
            .filter { it.hasAnnotation<ExceptionHandler>() }
            .forEach { function ->
                val exceptionHandler = function.findAnnotation<ExceptionHandler>()!!
                val bakedHandler = bakeAttachableExceptionHandler(exceptionHandler, function, obj)
                when (annotation.type) {
                    AdviceType.GLOBAL -> HQBukkitPlugin.GlobalExceptionHandlerRegistry.attachExceptionHandler(bakedHandler)
                    AdviceType.PLUGIN -> {
                        val plugin = PluginScopeFinder.find(obj::class) ?: throw NullPointerException("클래스가 위치한 플러그인을 찾을 수 없습니다.")
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
                val exceptionClass = function.valueParameters.singleOrNull { it.type.isSubtypeOf(Exception::class.starProjectedType) }
                    ?: throw IllegalStateException("ExceptionHandler 의 value parameter 에는 Exception 이 들어와야합니다.")
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