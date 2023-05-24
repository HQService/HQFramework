package kr.hqservice.framework.coroutine.dispatcher

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

/**
 * 디스패칭이 필요하지 않을 때를 구별합니다.
 */
@InternalCoroutinesApi
object BukkitMainDispatcherImmediate : MainCoroutineDispatcher(), Delay {
    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        BukkitMainDispatcher.dispatch(context, block)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        BukkitMainDispatcher.scheduleResumeAfterDelay(timeMillis, continuation)
    }
}