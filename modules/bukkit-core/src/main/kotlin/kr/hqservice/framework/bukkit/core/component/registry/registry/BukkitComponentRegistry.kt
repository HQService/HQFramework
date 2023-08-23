package kr.hqservice.framework.bukkit.core.component.registry.registry

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.registry.HQInstanceFactory
import kr.hqservice.framework.bukkit.core.component.registry.PluginDepend
import kr.hqservice.framework.bukkit.core.extension.getHQConfig
import kr.hqservice.framework.global.core.component.registry.JarBasedComponentRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import org.koin.core.qualifier.Qualifier
import java.util.jar.JarFile
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

class BukkitComponentRegistry(
    private val plugin: HQBukkitPlugin
) : JarBasedComponentRegistry(), InstanceFactoryRegistry {
    private companion object {
        val registeredInstanceFactories: MutableMap<KClass<*>, HQInstanceFactory<*>> = mutableMapOf()
    }

    override fun getComponentScope(): String {
        return plugin::class.java.packageName
    }

    override fun getJar(): JarFile {
        return JarFile(plugin.getJar())
    }

    override fun filterComponent(clazz: Class<*>): Boolean {
        val depend = clazz.annotations.filterIsInstance<PluginDepend>()
        if (depend.isEmpty()) {
            return true
        }
        depend.first().plugins.forEach { pluginId ->
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
            put(CoroutineScope::class, plugin)
            put(HQYamlConfiguration::class, getConfiguration())
        }
    }

    override fun injectProxy(
        kParameter: KParameter,
        qualifier: Qualifier?,
        scopeQualifier: Qualifier?
    ): Any? {
        for ((type, factory) in registeredInstanceFactories) {
            if (type.starProjectedType.classifier == kParameter.type.classifier) {
                return factory.createInstance(plugin, kParameter, qualifier, scopeQualifier)
            }
        }
        return null
    }

    override fun getConfiguration(): HQYamlConfiguration {
        return plugin.getHQConfig()
    }

    override fun <T> registerInstanceFactory(instanceFactory: HQInstanceFactory<T>) {
        val factoryType = instanceFactory::class.supertypes
            .first { it.isSubtypeOf(HQInstanceFactory::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure
        registeredInstanceFactories[factoryType] = instanceFactory
    }
}