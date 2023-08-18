package kr.hqservice.framework

import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.CoroutineScopeAdvice
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.ExceptionHandler
import kr.hqservice.framework.global.core.extension.print
import java.lang.reflect.InvocationTargetException

@CoroutineScopeAdvice
class TestAdvice {
    @ExceptionHandler
    fun advice(exception: InvocationTargetException) {
        exception.message.print()
    }
}