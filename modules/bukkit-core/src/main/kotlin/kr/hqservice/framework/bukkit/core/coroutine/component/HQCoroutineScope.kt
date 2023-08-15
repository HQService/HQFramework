package kr.hqservice.framework.bukkit.core.coroutine.component

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.global.core.component.HQComponent
import kotlin.coroutines.CoroutineContext

abstract class HQCoroutineScope(plugin: HQBukkitPlugin, private val dispatcher: CoroutineDispatcher) : CoroutineScope,
    HQComponent {
    private val supervisorJob = SupervisorJob()
    private val pluginCoroutineContextElement = PluginCoroutineContextElement(plugin)

    final override val coroutineContext: CoroutineContext
        get() = supervisorJob + dispatcher + pluginCoroutineContextElement + getExceptionHandler() + getCoroutineName()

    fun getSupervisor(): Job {
        return supervisorJob
    }

    protected abstract fun getExceptionHandler(): CoroutineExceptionHandler

    protected abstract fun getCoroutineName(): CoroutineName
}