package kr.hqservice.framework.bukkit.core.coroutine.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

val Job.coroutineContext: CoroutineContext
    get() {
        return (this as CoroutineScope).coroutineContext
    }