package kr.hqservice.framework.bukkit.core.scheduler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.scheduler.bukkit.HQBukkitScheduler
import kr.hqservice.framework.bukkit.core.scheduler.folia.HQFoliaGlobalScheduler
import kr.hqservice.framework.bukkit.core.scheduler.folia.HQFoliaRegionScheduler
import org.bukkit.Location
import org.bukkit.plugin.Plugin

private val isFolia: Boolean by lazy {
    runCatching { Class.forName("io.papermc.paper.threadedregions.RegionizedServer") }.isSuccess
}

fun Plugin.getScheduler(): HQScheduler {
    return if (this is HQBukkitPlugin) getScheduler()
    else if (isFolia) HQFoliaGlobalScheduler(this)
    else HQBukkitScheduler(this)
}

fun Plugin.getScheduler(location: Location): HQScheduler {
    return if (this is HQBukkitPlugin) getScheduler(location)
    else if (isFolia) HQFoliaRegionScheduler(this, location)
    else HQBukkitScheduler(this)
}