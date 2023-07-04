package kr.hqservice.framework.coroutine

import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class UUIDCoroutineContextElement(val uuid: UUID) : AbstractCoroutineContextElement(UUIDCoroutineContextElement) {
    companion object Key : CoroutineContext.Key<UUIDCoroutineContextElement>
}