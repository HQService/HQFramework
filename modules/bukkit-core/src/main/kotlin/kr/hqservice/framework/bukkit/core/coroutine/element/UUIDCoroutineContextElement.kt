package kr.hqservice.framework.bukkit.core.coroutine.element

import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class UUIDCoroutineContextElement(val uuid: UUID) : AbstractCoroutineContextElement(UUIDCoroutineContextElement) {
    companion object Key : CoroutineContext.Key<UUIDCoroutineContextElement>
}