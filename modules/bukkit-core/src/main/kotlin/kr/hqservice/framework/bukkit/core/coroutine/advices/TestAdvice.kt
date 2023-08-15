package kr.hqservice.framework.bukkit.core.coroutine.advices

import kr.hqservice.framework.bukkit.core.coroutine.component.AdviceType
import kr.hqservice.framework.bukkit.core.coroutine.component.CoroutineScopeAdvice
import kr.hqservice.framework.bukkit.core.coroutine.component.ExceptionHandler
import kr.hqservice.framework.bukkit.core.coroutine.component.MustBeStored
import kr.hqservice.framework.global.core.component.error.IllegalDependException
import kr.hqservice.framework.global.core.extension.print

@CoroutineScopeAdvice(AdviceType.GLOBAL)
class TestAdvice {
    @MustBeStored
    @ExceptionHandler
    fun testHandler(exception: IllegalDependException) {
        exception.message.print("받아옴!: ")
    }
}