package kr.hqservice.framework.bukkit.core.coroutine.element

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class TeardownOptionCoroutineContextElement(
    val cancelWhenPluginTeardown: Boolean
) : AbstractCoroutineContextElement(TeardownOptionCoroutineContextElement){
    companion object Key : CoroutineContext.Key<TeardownOptionCoroutineContextElement>
}