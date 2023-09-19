package kr.hqservice.framework.bukkit.core.util

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import org.bukkit.Bukkit
import kotlin.reflect.KClass

object PluginScopeFinder {
    fun find(kClass: KClass<*>): HQBukkitPlugin? {
        return Bukkit.getServer()
            .pluginManager
            .plugins
            .filterIsInstance<HQBukkitPlugin>()
            .singleOrNull { hqBukkitPlugin ->
                val targetPackage = kClass.java.packageName
                val pluginPackage = hqBukkitPlugin::class.java.packageName
                targetPackage == pluginPackage || targetPackage.startsWith("$pluginPackage.")
            }
    }

    fun get(kClass: KClass<*>): HQBukkitPlugin {
        return find(kClass) ?: throw NullPointerException("cannot find plugin of KClass ${kClass.simpleName} located.")
    }
}