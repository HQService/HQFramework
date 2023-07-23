package kr.hqservice.framework.bukkit.core.component

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.component.HQComponent
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KAnnotatedElement

interface HQInstanceFactory<T> : HQComponent {
    fun createInstance(plugin: HQBukkitPlugin, annotatedElement: KAnnotatedElement, qualifier: Qualifier?, scopeQualifier: Qualifier?): T
}