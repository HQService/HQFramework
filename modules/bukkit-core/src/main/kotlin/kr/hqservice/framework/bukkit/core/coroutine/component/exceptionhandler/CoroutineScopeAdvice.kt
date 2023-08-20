package kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler

import kr.hqservice.framework.global.core.component.Scannable

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Scannable
annotation class CoroutineScopeAdvice(val type: AdviceType = AdviceType.PLUGIN)
