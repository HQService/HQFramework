package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object LifecycleMainThread {
    @Volatile
    private var loop: CoroutineDispatcher? = null
    private var depth = 0

    val current: CoroutineDispatcher? get() = loop

    val isBlocked: Boolean get() = loop != null

    fun <T> runBlockingOnMainThread(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T =
        runBlocking(context.minusKey(ContinuationInterceptor)) {
            val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
            enter(dispatcher)
            try {
                block()
            } finally {
                exit()
            }
        }

    @Synchronized
    private fun enter(dispatcher: CoroutineDispatcher) {
        if (depth++ == 0) loop = dispatcher
    }

    @Synchronized
    private fun exit() {
        if (--depth == 0) loop = null
    }
}
