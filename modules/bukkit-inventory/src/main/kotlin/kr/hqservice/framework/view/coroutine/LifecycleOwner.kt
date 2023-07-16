package kr.hqservice.framework.view.coroutine

import kotlinx.coroutines.CoroutineScope

interface LifecycleOwner : CoroutineScope {
    fun dispose()
}