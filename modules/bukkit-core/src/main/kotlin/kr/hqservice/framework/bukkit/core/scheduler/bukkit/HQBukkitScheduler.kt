package kr.hqservice.framework.bukkit.core.scheduler.bukkit

import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.plugin.Plugin

class HQBukkitScheduler(
    private val plugin: Plugin
) : HQScheduler {
    private val bukkitScheduler = plugin.server.scheduler

    override fun runTask(runnable: () -> Unit) {
        bukkitScheduler.runTask(plugin, Runnable {
            runnable.invoke()
        })
    }

    override fun runTaskLater(delay: Long, runnable: () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskLater(plugin, Runnable {
            runnable.invoke()
        }, delay)

        return HQBukkitTask(task)
    }

    override fun runTaskTimer(delay: Long, period: Long, runnable: () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskTimer(plugin, Runnable {
            runnable.invoke()
        }, delay, period)

        return HQBukkitTask(task)
    }

    override fun runTaskAsynchronously(runnable: () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskAsynchronously(plugin, Runnable {
            runnable.invoke()
        })

        return HQBukkitTask(task)
    }

    override fun runTaskLaterAsynchronously(delay: Long, runnable: () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskLaterAsynchronously(plugin, Runnable {
            runnable.invoke()
        }, delay)

        return HQBukkitTask(task)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long, runnable: () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskTimerAsynchronously(plugin, Runnable {
            runnable.invoke()
        }, delay, period)

        return HQBukkitTask(task)
    }
}