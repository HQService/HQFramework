package kr.hqservice.framework.inventory.coroutine

import kotlinx.coroutines.CoroutineScope

interface LifecycleOwner : CoroutineScope {
    fun dispose()
}