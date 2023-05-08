package kr.hqservice.framework.coroutine.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kr.hqservice.framework.core.component.HQComponent

interface HQCoroutineContext : CoroutineScope, HQComponent {
    fun getSupervisor(): Job
}