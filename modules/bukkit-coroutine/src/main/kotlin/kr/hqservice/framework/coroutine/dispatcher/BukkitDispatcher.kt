package kr.hqservice.framework.coroutine.dispatcher

import kotlinx.coroutines.*
import kr.hqservice.framework.coroutine.element.PluginCoroutineContextElement
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.coroutines.CoroutineContext

@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class BukkitDispatcher(private val isAsync: Boolean) : MainCoroutineDispatcher(), Delay {
    override val immediate: MainCoroutineDispatcher
        get() = BukkitMainDispatcherImmediate()

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
        val task = if (isAsync) {
            runnable.runTaskLaterAsynchronously(plugin, timeMillis / 50)
        } else {
            runnable.runTaskLater(plugin, timeMillis / 50)
        }
        continuation.invokeOnCancellation { task.cancel() }
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