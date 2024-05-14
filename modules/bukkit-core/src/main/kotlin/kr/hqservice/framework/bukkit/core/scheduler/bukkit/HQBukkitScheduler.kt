package kr.hqservice.framework.bukkit.core.scheduler.bukkit

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.plugin.Plugin

class HQBukkitScheduler(
    private val plugin: Plugin
) : HQScheduler {
    private val bukkitScheduler = plugin.server.scheduler

    override fun runTask(runnable: suspend () -> Unit): HQTask {
        val task = bukkitScheduler.runTask(plugin, Runnable {
            runBlocking {
                runnable.invoke()
            }
        })

        return HQBukkitTask(task)
    }

    override fun runTaskLater(delay: Long, runnable: suspend () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskLater(plugin, Runnable {
            runBlocking {
                runnable.invoke()
            }
        }, delay)

        return HQBukkitTask(task)
    }

    override fun runTaskTimer(delay: Long, period: Long, runnable: suspend () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskTimer(plugin, Runnable {
            runBlocking {
                runnable.invoke()
            }
        }, delay, period)

        return HQBukkitTask(task)
    }

    override fun runTaskAsynchronously(runnable: suspend () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskAsynchronously(plugin, Runnable {
            runBlocking {
                runnable.invoke()
            }
        })

        return HQBukkitTask(task)
    }

    override fun runTaskLaterAsynchronously(delay: Long, runnable: suspend () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskLaterAsynchronously(plugin, Runnable {
            runBlocking {
                runnable.invoke()
            }
        }, delay)

        return HQBukkitTask(task)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long, runnable: suspend () -> Unit): HQTask {
        val task = bukkitScheduler.runTaskTimerAsynchronously(plugin, Runnable {
            runBlocking {
                runnable.invoke()
            }
        }, delay, period)

        return HQBukkitTask(task)
    }
}