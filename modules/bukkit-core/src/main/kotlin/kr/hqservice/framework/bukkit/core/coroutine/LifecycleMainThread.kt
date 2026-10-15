package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import org.bukkit.plugin.Plugin
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object LifecycleMainThread {
    private class Blocking(val plugin: Plugin?, val loop: CoroutineDispatcher)

    private val stack = ArrayDeque<Blocking>()

    @Volatile
    private var top: Blocking? = null

    val isBlocked: Boolean get() = top != null

    fun loopFor(plugin: Plugin): CoroutineDispatcher? {
        val current = top ?: return null
        return if (current.plugin == null || current.plugin === plugin) current.loop else null
    }

    fun <T> runBlockingOnMainThread(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T =
        runBlocking(context.minusKey(ContinuationInterceptor)) {
            val loop = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
            val blocking = Blocking(context[PluginCoroutineContextElement]?.plugin, loop)
            enter(blocking)
            try {
                block()
            } finally {
                exit(blocking)
            }
        }

    @Synchronized
    private fun enter(blocking: Blocking) {
        stack.addLast(blocking)
        top = blocking
    }

    @Synchronized
    private fun exit(blocking: Blocking) {
        stack.remove(blocking)
        top = stack.lastOrNull()
    }
}
