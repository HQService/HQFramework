package kr.hqservice.framework.bungee.core.component.registry

import kr.hqservice.framework.bungee.core.HQBungeePlugin
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import kr.hqservice.framework.proxy.core.component.registry.ProxyComponentRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import net.md_5.bungee.api.plugin.Plugin
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KClass

@Factory(binds = [ComponentRegistry::class])
class BungeeComponentRegistry(@InjectedParam private val plugin: HQBungeePlugin) : ProxyComponentRegistry<HQBungeePlugin>(plugin) {
    override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> {
        return mutableMapOf<KClass<*>, Any>().apply {
            put(Plugin::class, plugin)
            put(HQProxyPlugin::class, plugin)
            put(HQBungeePlugin::class, plugin)
            put(HQPlugin::class, plugin)
            put(plugin::class, plugin)
            put(Logger::class, plugin.logger)
            put(HQYamlConfiguration::class, getConfiguration())
        }
    }

    override fun getConfiguration(): HQYamlConfiguration {
        return File(plugin.dataFolder, "config.yml").yaml()
    }
}