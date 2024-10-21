package kr.hqservice.framework.bukkit.core.scheduler.folia

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.scheduler.HQScheduler
import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.Location
import java.util.concurrent.TimeUnit

class HQFoliaRegionScheduler(
    private val plugin: HQBukkitPlugin,
    private val location: Location
) : HQScheduler {
    private val regionScheduler = plugin.server.regionScheduler
    private val asyncScheduler = plugin.server.asyncScheduler

    override fun runTask(runnable: () -> Unit) {
        regionScheduler.execute(plugin, location) {
            runnable.invoke()
        }
    }

    override fun runTaskLater(delay: Long, runnable: () -> Unit): HQTask {
        val task = regionScheduler.runDelayed(plugin, location, {
            runBlocking {
                runnable.invoke()
            }
        }, delay)

        return HQFoliaTask(task)
    }

    override fun runTaskTimer(delay: Long, period: Long, runnable: () -> Unit): HQTask {
        val task = regionScheduler.runAtFixedRate(plugin, location, {
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