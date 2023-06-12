package kr.hqservice.framework.coroutine.dispatcher

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.coroutine.BukkitCoroutineContextElement
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import kotlin.coroutines.CoroutineContext

@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
object BukkitMainDispatcher : MainCoroutineDispatcher(), Delay {
    override val immediate: MainCoroutineDispatcher
        get() = BukkitMainDispatcherImmediate

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val plugin = getPluginByCoroutineContext(context)
        Bukkit.getScheduler().runTask(plugin, block)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val plugin = getPluginByCoroutineContext(continuation.context)
        val runnable = object : BukkitRunnable() {
            override fun run() {
                with(continuation) {
                    resumeUndispatched(Unit)
                }
            }
        }
        val task = runnable.runTaskLater(plugin, timeMillis / 50)
        continuation.invokeOnCancellation { task.cancel() }
    }

    private fun getPluginByCoroutineContext(coroutineContext: CoroutineContext): HQBukkitPlugin {
        val plugin = coroutineContext[BukkitCoroutineContextElement]?.plugin
        return plugin
            ?: throw IllegalStateException("BukkitMainCoroutineContext 를 사용하기 위해서는, CombineContext 에 BukkitCoroutineContextElement 를 넣어주세요.")
    }
}

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