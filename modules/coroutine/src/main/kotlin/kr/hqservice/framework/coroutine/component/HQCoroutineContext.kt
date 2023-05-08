package kr.hqservice.framework.coroutine.component

import kotlinx.coroutines.*
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.HQComponent
import kr.hqservice.framework.coroutine.BukkitCoroutineContextElement
import kotlin.coroutines.CoroutineContext

abstract class HQCoroutineContext(plugin: HQPlugin, private val dispatcher: CoroutineDispatcher) : CoroutineScope, HQComponent {
    private val supervisorJob = SupervisorJob()
    private val bukkitCoroutineContextElement = BukkitCoroutineContextElement(plugin)

    final override val coroutineContext: CoroutineContext
        get() = supervisorJob + dispatcher + bukkitCoroutineContextElement + getExceptionHandler() + getCoroutineName()

    fun getSupervisor(): Job {
        return supervisorJob
    }

    protected abstract fun getExceptionHandler(): CoroutineExceptionHandler

    protected abstract fun getCoroutineName(): CoroutineName
}