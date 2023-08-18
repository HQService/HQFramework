package kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ExceptionHandler(val priority: Int = 999)