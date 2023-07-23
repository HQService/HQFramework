package kr.hqservice.framework.bukkit.core.component

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import org.koin.core.qualifier.Qualifier

interface InstanceFactory<T> {
    fun createInstance(plugin: HQBukkitPlugin, qualifier: Qualifier?, scopeQualifier: Qualifier?): T
}