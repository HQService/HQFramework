package kr.hqservice.framework.bukkit.core.scheduler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.scheduler.bukkit.HQBukkitScheduler
import org.bukkit.Location
import org.bukkit.plugin.Plugin

fun Plugin.getScheduler(): HQScheduler {
    return if (this is HQBukkitPlugin) getScheduler()
    else HQBukkitScheduler(this)
}

fun Plugin.getScheduler(location: Location): HQScheduler {
    return if (this is HQBukkitPlugin) getScheduler(location)
    else HQBukkitScheduler(this)
}