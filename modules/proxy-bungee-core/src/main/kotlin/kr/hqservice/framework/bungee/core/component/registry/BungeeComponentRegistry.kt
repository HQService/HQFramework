package kr.hqservice.framework.bungee.core.component.registry

import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.global.core.component.registry.JarBasedComponentRegistry
import net.md_5.bungee.api.plugin.Plugin
import org.koin.core.annotation.Factory
import java.util.jar.JarFile
import java.util.logging.Logger
import kotlin.reflect.KClass

@Factory(binds = [ComponentRegistry::class])
class BungeeComponentRegistry(private val plugin: HQPlugin) : JarBasedComponentRegistry() {
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
            put(HQPlugin::class, plugin)
            put(Logger::class, plugin.getLogger())
        }
    }
}