package kr.hqservice.framework.velocity.core.component.registry

import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import kr.hqservice.framework.proxy.core.component.registry.ProxyComponentRegistry
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KClass

@Factory(binds = [ComponentRegistry::class])
class VelocityComponentRegistry(@InjectedParam private val plugin: HQVelocityPlugin) :
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