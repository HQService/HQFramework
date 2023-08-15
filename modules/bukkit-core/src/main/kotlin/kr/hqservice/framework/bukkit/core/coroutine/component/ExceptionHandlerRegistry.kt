package kr.hqservice.framework.bukkit.core.coroutine.component

interface ExceptionHandlerRegistry {
    fun attachExceptionHandler(attachableExceptionHandler: AttachableExceptionHandler)
}