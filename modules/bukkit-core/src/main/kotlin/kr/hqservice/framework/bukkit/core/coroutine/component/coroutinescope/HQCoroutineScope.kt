package kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.global.core.component.HQComponent
import kotlin.coroutines.CoroutineContext

abstract class HQCoroutineScope(
    private val plugin: HQBukkitPlugin,
    private val dispatcher: CoroutineDispatcher
) : CoroutineScope, HQComponent {
    private val supervisorJob = SupervisorJob()

    final override val coroutineContext: CoroutineContext
        get() = supervisorJob + dispatcher + plugin.coroutineContext[PluginCoroutineContextElement.Key]!! + plugin.coroutineContext[CoroutineExceptionHandler.Key]!! + getCoroutineName()

    fun getSupervisor(): Job {
        return supervisorJob
    }

    open fun getCoroutineName(): CoroutineName {
        return CoroutineName("UnnamedHQCoroutineScope")
    }
}