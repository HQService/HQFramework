package kr.hqservice.framework.velocity.core.component.registry

import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import kr.hqservice.framework.proxy.core.component.registry.ProxyComponentRegistry
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KClass

class VelocityComponentRegistry(private val plugin: HQVelocityPlugin) :
    ProxyComponentRegistry<HQVelocityPlugin>(plugin) {
    override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> {
        return mutableMapOf<KClass<*>, Any>().apply {
            put(HQProxyPlugin::class, plugin)
            put(HQPlugin::class, plugin)
            put(plugin::class, plugin)
            put(Logger::class, plugin.getLogger())
            put(HQYamlConfiguration::class, getConfiguration())
        }
    }

    override fun getConfiguration(): HQYamlConfiguration {
        return File(plugin.getDataFolder(), "config.yml").yaml()
    }
}