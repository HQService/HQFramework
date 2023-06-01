package kr.hqservice.framework.bukkit.core.component.registry

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.global.core.component.registry.JarBasedComponentRegistry
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import org.koin.core.annotation.Factory
import java.util.jar.JarFile
import java.util.logging.Logger
import kotlin.reflect.KClass

@Factory(binds = [ComponentRegistry::class])
class BukkitComponentRegistry(private val plugin: HQBukkitPlugin) : JarBasedComponentRegistry() {
    override fun getComponentScope(): String {
        return plugin::class.java.packageName
    }

    override fun getJar(): JarFile {
        return JarFile(plugin.getJar())
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