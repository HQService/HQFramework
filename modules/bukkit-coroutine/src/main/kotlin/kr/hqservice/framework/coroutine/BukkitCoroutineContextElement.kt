package kr.hqservice.framework.coroutine

import kr.hqservice.framework.core.HQBukkitPlugin
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class BukkitCoroutineContextElement(val plugin: HQBukkitPlugin) : AbstractCoroutineContextElement(BukkitCoroutineContextElement) {
    companion object Key : CoroutineContext.Key<BukkitCoroutineContextElement>
}