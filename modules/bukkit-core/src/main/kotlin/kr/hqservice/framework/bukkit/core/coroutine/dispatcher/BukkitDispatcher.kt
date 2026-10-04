package kr.hqservice.framework.bukkit.core.coroutine.dispatcher

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.LifecycleMainThread
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

internal fun ticksFor(timeMillis: Long): Long = maxOf(1L, (timeMillis + 49) / 50)

@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class BukkitDispatcher(private val isAsync: Boolean, private val location: Location?) : MainCoroutineDispatcher(), Delay {
    override val immediate: MainCoroutineDispatcher
        get() = BukkitMainDispatcherImmediate()

    private val isMainThreadDispatcher: Boolean get() = !isAsync && location == null

    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        !isMainThreadDispatcher || !Bukkit.isPrimaryThread() || lifecycleLoop(context) == null

    private fun lifecycleLoop(context: CoroutineContext): CoroutineDispatcher? {
        if (!isMainThreadDispatcher || !LifecycleMainThread.isBlocked) return null
        return LifecycleMainThread.loopFor(getPluginByCoroutineContext(context))
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        lifecycleLoop(context)?.let { loop ->
            loop.dispatch(context) { if (Bukkit.isPrimaryThread()) block.run() else dispatchThroughScheduler(context, block) }
            return
        }
        dispatchThroughScheduler(context, block)
    }

    private fun dispatchThroughScheduler(context: CoroutineContext, block: Runnable) {
        val plugin = getPluginByCoroutineContext(context)
        try {
            if (location != null) {
                if (isAsync) plugin.getScheduler(location).runTaskAsynchronously { block.run() }
                else plugin.getScheduler(location).runTask { block.run() }
            }
            else {
                if (isAsync) plugin.getScheduler().runTaskAsynchronously { block.run() }
                else PendingMainThreadWork.schedule(plugin, plugin.getScheduler(), block)
            }
        } catch (_: IllegalPluginAccessException) {
            context[Job]?.cancel(CancellationException("Plugin is disabled, cannot dispatch"))
            Dispatchers.IO.dispatch(context, block)
        }
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val plugin = getPluginByCoroutineContext(continuation.context)
        (lifecycleLoop(continuation.context) as? Delay)?.let { loop ->
            val handle = loop.invokeOnTimeout(timeMillis, { continuation.resume(Unit) }, continuation.context)
            continuation.invokeOnCancellation { handle.dispose() }
            return
        }
        if (isMainThreadDispatcher) PendingMainThreadWork.track(plugin, continuation)

        val task = try {
            val resumer: () -> Unit = {
                PendingMainThreadWork.untrack(plugin, continuation)
                with(continuation) { resumeUndispatched(Unit) }
            }
            val ticks = ticksFor(timeMillis)
            val scheduler = if (location != null) plugin.getScheduler(location) else plugin.getScheduler()
            if (isAsync) scheduler.runTaskLaterAsynchronously(ticks, resumer)
            else scheduler.runTaskLater(ticks, resumer)
        } catch (_: IllegalPluginAccessException) {
            continuation.cancel(CancellationException("Plugin is disabled, cannot resume delay"))
            null
        }

        continuation.invokeOnCancellation {
            PendingMainThreadWork.untrack(plugin, continuation)
            task?.cancel()
        }
    }

    companion object {
        fun drainPending(plugin: Plugin) = PendingMainThreadWork.drain(plugin)
    }

    private fun getPluginByCoroutineContext(coroutineContext: CoroutineContext): Plugin {
        val plugin = coroutineContext[PluginCoroutineContextElement]?.plugin
        return plugin ?: Bukkit.getPluginManager().getPlugin("HQFramework")!!
    }

    private inner class BukkitMainDispatcherImmediate : MainCoroutineDispatcher(), Delay {
        override val immediate: MainCoroutineDispatcher
            get() = this

        override fun isDispatchNeeded(context: CoroutineContext): Boolean {
            return !Bukkit.isPrimaryThread()
        }

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            this@BukkitDispatcher.dispatch(context, block)
        }

        override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
            this@BukkitDispatcher.scheduleResumeAfterDelay(timeMillis, continuation)
        }
    }
}
