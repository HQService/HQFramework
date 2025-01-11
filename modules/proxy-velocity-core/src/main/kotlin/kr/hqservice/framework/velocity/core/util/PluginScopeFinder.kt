package kr.hqservice.framework.velocity.core.util

import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kotlin.reflect.KClass

object PluginScopeFinder {
    fun find(kClass: KClass<*>): HQVelocityPlugin? {
        return HQVelocityPlugin.proxyServer
            ?.pluginManager
            ?.plugins
            ?.mapNotNull {
                it.instance.get() as? HQVelocityPlugin
            }?.filter { hqVelocityPlugin ->
                val targetPackage = kClass.java.packageName
                val pluginPackage = hqVelocityPlugin::class.java.packageName
                targetPackage == pluginPackage || targetPackage.startsWith("$pluginPackage.")
            }?.maxByOrNull { hqVelocityPlugin ->
                hqVelocityPlugin::class.java.packageName.length
            }
    }

    fun get(kClass: KClass<*>): HQVelocityPlugin {
        return find(kClass)
            ?: throw NullPointerException("cannot find plugin of KClass ${kClass.simpleName} located.")
    }
}