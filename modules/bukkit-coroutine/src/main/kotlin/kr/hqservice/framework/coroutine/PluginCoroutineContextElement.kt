package kr.hqservice.framework.coroutine

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class PluginCoroutineContextElement(val plugin: HQBukkitPlugin) : AbstractCoroutineContextElement(PluginCoroutineContextElement) {
    companion object Key : CoroutineContext.Key<PluginCoroutineContextElement>
}