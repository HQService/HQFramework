package kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler

interface ExceptionHandlerRegistry {
    fun attachExceptionHandler(attachableExceptionHandler: AttachableExceptionHandler)
}