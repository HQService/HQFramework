package kr.hqservice.framework.coroutine

import kr.hqservice.framework.core.HQPlugin
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class BukkitCoroutineContextElement(val plugin: HQPlugin) : AbstractCoroutineContextElement(BukkitCoroutineContextElement) {
    companion object Key : CoroutineContext.Key<BukkitCoroutineContextElement>
}