package kr.hqservice.framework.bungee.core.component.registry

import kr.hqservice.framework.bungee.core.HQBungeePlugin
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import kr.hqservice.framework.proxy.core.component.registry.ProxyComponentRegistry
import org.koin.core.annotation.Factory
import java.util.logging.Logger
import kotlin.reflect.KClass

@Factory(binds = [ComponentRegistry::class])
class BungeeComponentRegistry(private val plugin: HQBungeePlugin) : ProxyComponentRegistry<HQBungeePlugin>(plugin) {
    override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> {
        return mutableMapOf<KClass<*>, Any>().apply {
            put(HQProxyPlugin::class, plugin)
            put(HQBungeePlugin::class, plugin)
            put(HQPlugin::class, plugin)
            put(plugin::class, plugin)
            put(Logger::class, plugin.logger)
        }
    }
}