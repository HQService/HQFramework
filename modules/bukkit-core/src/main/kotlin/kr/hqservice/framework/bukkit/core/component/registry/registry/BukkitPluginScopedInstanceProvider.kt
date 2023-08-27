package kr.hqservice.framework.bukkit.core.component.registry.registry

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.extension.getHQConfig
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import java.util.logging.Logger
import kotlin.reflect.KClass

object BukkitPluginScopedInstanceProvider {
    fun provideInstance(plugin: HQBukkitPlugin): MutableMap<KClass<*>, Any> {
        return mutableMapOf<KClass<*>, Any>().apply {
            put(Plugin::class, plugin)
            put(plugin::class, plugin)
            put(HQBukkitPlugin::class, plugin)
            put(Logger::class, plugin.logger)
            put(ConfigurationSection::class, plugin.config)
            put(CoroutineScope::class, plugin)
            put(HQYamlConfiguration::class, plugin.getHQConfig())
        }
    }
}