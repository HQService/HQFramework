package kr.hqservice.framework.bukkit.core.coroutine.dispatcher

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

internal object PendingMainThreadWork {
    private val blocks = ConcurrentHashMap<Plugin, MutableSet<Runnable>>()
    private val delays = ConcurrentHashMap<Plugin, MutableSet<CancellableContinuation<Unit>>>()

    fun schedule(plugin: Plugin, scheduler: HQScheduler, block: Runnable) {
        val pending = blocks.getOrPut(plugin) { ConcurrentHashMap.newKeySet() }
        val once = AtomicBoolean()
        lateinit var wrapper: Runnable
        wrapper = Runnable {
            if (once.compareAndSet(false, true)) {
                pending.remove(wrapper)
                block.run()
            }
        }
        pending += wrapper
        scheduler.runTask { wrapper.run() }
    }

    fun track(plugin: Plugin, continuation: CancellableContinuation<Unit>) {
        delays.getOrPut(plugin) { ConcurrentHashMap.newKeySet() } += continuation
    }

    fun untrack(plugin: Plugin, continuation: CancellableContinuation<Unit>) {
        delays[plugin]?.remove(continuation)
    }

    fun drain(plugin: Plugin) {
        blocks.remove(plugin)?.toList()?.forEach { it.run() }
        delays.remove(plugin)?.toList()?.forEach { it.cancel(CancellationException("Plugin is disabling, delayed work is cancelled")) }
    }
}
