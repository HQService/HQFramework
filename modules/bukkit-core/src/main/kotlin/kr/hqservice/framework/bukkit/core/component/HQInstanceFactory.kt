package kr.hqservice.framework.bukkit.core.component

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.component.HQComponent
import org.koin.core.qualifier.Qualifier

interface HQInstanceFactory<T> : HQComponent {
    fun createInstance(plugin: HQBukkitPlugin, qualifier: Qualifier?, scopeQualifier: Qualifier?): T
}