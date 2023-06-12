package kr.hqservice.framework.bukkit.core.component.registry

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.PluginDepend
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.global.core.component.registry.JarBasedComponentRegistry
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.koin.core.annotation.Factory
import java.util.jar.JarFile
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Factory(binds = [ComponentRegistry::class])
class BukkitComponentRegistry(
    private val plugin: HQBukkitPlugin
) : JarBasedComponentRegistry() {
    override fun getComponentScope(): String {
        return plugin::class.java.packageName
    }

    override fun getJar(): JarFile {
        return JarFile(plugin.getJar())
    }

    override fun filterComponent(clazz: Class<*>): Boolean {
        val depend = clazz.kotlin.findAnnotation<PluginDepend>() ?: return true
        depend.plugins.forEach { pluginId ->
            val plugin = plugin.server.pluginManager.getPlugin(pluginId)
            if (plugin == null) {
                this.plugin.logger.info("cannot find depending plugin with id $pluginId, excluding component ${clazz.simpleName}")
                return false
            }
        }
        return true
    }

    override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> {
        return mutableMapOf<KClass<*>, Any>().apply {
            put(Plugin::class, plugin)
            put(plugin::class, plugin)
            put(HQBukkitPlugin::class, plugin)
            put(Logger::class, plugin.logger)
            put(ConfigurationSection::class, plugin.config)
        }
    }
}