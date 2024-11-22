package kr.hqservice.framework.bungee.core.util

import kr.hqservice.framework.bungee.core.HQBungeePlugin
import net.md_5.bungee.api.ProxyServer
import kotlin.reflect.KClass

object PluginScopeFinder {
    fun find(kClass: KClass<*>): HQBungeePlugin? {
        return ProxyServer.getInstance()
            ?.pluginManager
            ?.plugins
            ?.filterIsInstance<HQBungeePlugin>()
            ?.filter { hqBungeePlugin ->
                val targetPackage = kClass.java.packageName
                val pluginPackage = hqBungeePlugin::class.java.packageName
                targetPackage == pluginPackage || targetPackage.startsWith("$pluginPackage.")
            }?.maxByOrNull { hqVelocityPlugin ->
                hqVelocityPlugin::class.java.packageName.length
            }
    }

    fun get(kClass: KClass<*>): HQBungeePlugin {
        return find(kClass)
            ?: throw NullPointerException("cannot find plugin of KClass ${kClass.simpleName} located.")
    }
}