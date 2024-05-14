package kr.hqservice.framework.bukkit.core.scheduler.folia

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class HQFoliaGlobalScheduler(
    private val plugin: Plugin
) : HQScheduler {
    private val globalScheduler = plugin.server.globalRegionScheduler
    private val asyncScheduler = plugin.server.asyncScheduler

    override fun runTask(runnable: suspend () -> Unit): HQTask {
        val task = globalScheduler.run(plugin) {
            runBlocking {
                runnable.invoke()
            }
        }

        return HQFoliaTask(task)
    }

    override fun runTaskLater(delay: Long, runnable: suspend () -> Unit): HQTask {
        val task = globalScheduler.runDelayed(plugin, {
            runBlocking {
                runnable.invoke()
            }
        }, delay)

        return HQFoliaTask(task)
    }

    override fun runTaskTimer(delay: Long, period: Long, runnable: suspend () -> Unit): HQTask {
        val task = globalScheduler.runAtFixedRate(plugin, {
            runBlocking {
                runnable.invoke()
            }
        }, delay, period)

        return HQFoliaTask(task)
    }

    override fun runTaskAsynchronously(runnable: suspend () -> Unit): HQTask {
        val task = asyncScheduler.runNow(plugin) {
            runBlocking {
                runnable.invoke()
            }
        }

        return HQFoliaTask(task)
    }

    override fun runTaskLaterAsynchronously(delay: Long, runnable: suspend () -> Unit): HQTask {
        val task = asyncScheduler.runDelayed(plugin, {
            runBlocking {
                runnable.invoke()
            }
        }, delay * 50, TimeUnit.MILLISECONDS)

        return HQFoliaTask(task)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long, runnable: suspend () -> Unit): HQTask {
        val task = asyncScheduler.runAtFixedRate(plugin, {
            runBlocking {
                runnable.invoke()
            }
        }, delay * 50, period * 50, TimeUnit.MILLISECONDS)

        return HQFoliaTask(task)
    }
}