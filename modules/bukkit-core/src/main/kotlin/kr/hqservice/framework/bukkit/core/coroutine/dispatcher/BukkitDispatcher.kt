package kr.hqservice.framework.bukkit.core.coroutine.dispatcher

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class BukkitDispatcher(private val isAsync: Boolean, private val location: Location?) : MainCoroutineDispatcher(), Delay {
    override val immediate: MainCoroutineDispatcher
        get() = BukkitMainDispatcherImmediate()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val plugin = getPluginByCoroutineContext(context)
        try {
            if (location != null) {
                if (isAsync) plugin.getScheduler(location).runTaskAsynchronously { block.run() }
                else plugin.getScheduler(location).runTask { block.run() }
            }
            else {
                if (isAsync) plugin.getScheduler().runTaskAsynchronously { block.run() }
                else plugin.getScheduler().runTask { block.run() }
            }
        } catch (_: IllegalPluginAccessException) {}
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val plugin = getPluginByCoroutineContext(continuation.context)

        val task = if (location != null) {
            if (isAsync) {
                try {
                    plugin.getScheduler(location).runTaskLaterAsynchronously(timeMillis / 50) {
                        with(continuation) {
                            resumeUndispatched(Unit)
                        }
                    }
                } catch (_: IllegalPluginAccessException) { null }
            } else {
                try {
                    plugin.getScheduler(location).runTaskLater(timeMillis / 50) {
                        with(continuation) {
                            resumeUndispatched(Unit)
                        }
                    }
                } catch (_: IllegalPluginAccessException) { null }
            }
        } else {
            if (isAsync) {
                try {
                    plugin.getScheduler().runTaskLaterAsynchronously(timeMillis / 50) {
                        with(continuation) {
                            resumeUndispatched(Unit)
                        }
                    }
                } catch (_: IllegalPluginAccessException) { null }
            } else {
                try {
                    plugin.getScheduler().runTaskLater(timeMillis / 50) {
                        with(continuation) {
                            resumeUndispatched(Unit)
                        }
                    }
                } catch (_: IllegalPluginAccessException) { null }
            }
        }

        continuation.invokeOnCancellation { task?.cancel() }
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