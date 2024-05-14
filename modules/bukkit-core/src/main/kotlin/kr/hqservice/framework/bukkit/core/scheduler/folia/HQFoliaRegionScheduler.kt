package kr.hqservice.framework.bukkit.core.scheduler.folia

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class HQFoliaRegionScheduler(
    private val plugin: Plugin,
    private val location: Location
) : HQScheduler {
    private val regionScheduler = plugin.server.regionScheduler
    private val asyncScheduler = plugin.server.asyncScheduler

    override fun runTask(runnable: suspend () -> Unit): HQTask {
        val task = regionScheduler.run(plugin, location) {
            runBlocking {
                runnable.invoke()
            }
        }

        return HQFoliaTask(task)
    }

    override fun runTaskLater(delay: Long, runnable: suspend () -> Unit): HQTask {
        val task = regionScheduler.runDelayed(plugin, location, {
            runBlocking {
                runnable.invoke()
            }
        }, delay)

        return HQFoliaTask(task)
    }

    override fun runTaskTimer(delay: Long, period: Long, runnable: suspend () -> Unit): HQTask {
        val task = regionScheduler.runAtFixedRate(plugin, location, {
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