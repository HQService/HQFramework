package kr.hqservice.framework.bukkit.core.scheduler.folia

import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class HQFoliaGlobalScheduler(
    private val plugin: Plugin
) : HQScheduler {
    private val globalScheduler = plugin.server.globalRegionScheduler
    private val asyncScheduler = plugin.server.asyncScheduler

    override fun runTask(runnable: () -> Unit) {
        globalScheduler.execute(plugin) {
            runnable.invoke()
        }
    }

    override fun runTaskLater(delay: Long, runnable: () -> Unit): HQTask {
        val task = globalScheduler.runDelayed(plugin, {
            runnable.invoke()
        }, delay)

        return HQFoliaTask(task)
    }

    override fun runTaskTimer(delay: Long, period: Long, runnable: () -> Unit): HQTask {
        val task = globalScheduler.runAtFixedRate(plugin, {
            runnable.invoke()
        }, delay, period)

        return HQFoliaTask(task)
    }

    override fun runTaskAsynchronously(runnable: () -> Unit): HQTask {
        val task = asyncScheduler.runNow(plugin) {
            runnable.invoke()
        }

        return HQFoliaTask(task)
    }

    override fun runTaskLaterAsynchronously(delay: Long, runnable: () -> Unit): HQTask {
        val task = asyncScheduler.runDelayed(plugin, {
            runnable.invoke()
        }, delay * 50, TimeUnit.MILLISECONDS)

        return HQFoliaTask(task)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long, runnable: () -> Unit): HQTask {
        val task = asyncScheduler.runAtFixedRate(plugin, {
            runnable.invoke()
        }, delay * 50, period * 50, TimeUnit.MILLISECONDS)

        return HQFoliaTask(task)
    }
}