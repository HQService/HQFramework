package kr.hqservice.framework.bukkit.core.component.registry.registry

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.registry.HQInstanceFactory
import kr.hqservice.framework.bukkit.core.component.registry.PluginDepend
import kr.hqservice.framework.global.core.component.registry.JarBasedComponentRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.koin.core.qualifier.Qualifier
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

class BukkitComponentRegistry(
    private val plugin: HQBukkitPlugin
) : JarBasedComponentRegistry(), InstanceFactoryRegistry {
    private val registeredInstanceFactories: MutableMap<KClass<*>, HQInstanceFactory<*>> = mutableMapOf()

    override fun getComponentScope(): String {
        return plugin::class.java.packageName
    }

    override fun getJar(): File {
        return plugin.getJar()
    }

    override fun getPluginClassLoader(): ClassLoader {
        return plugin.getPluginClassLoader()
    }

    override fun filterComponent(clazz: Class<*>): Boolean {
        val depend = clazz.annotations.filterIsInstance<PluginDepend>()
        if (depend.isEmpty()) {
            return true
        }
        depend.first().plugins.forEach { pluginId ->
            plugin.server.pluginManager.getPlugin(pluginId) ?: return false
        }
        return true
    }

    override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> {
        return BukkitPluginScopedInstanceProvider.provideInstance(plugin).apply {
            put(InstanceFactoryRegistry::class, this@BukkitComponentRegistry)
        }
    }

    override fun injectProxy(
        kParameter: KParameter,
        qualifier: Qualifier?,
        scopeQualifier: Qualifier?
    ): Any? {
        val factory = findInstanceFactory(kParameter)
            ?: (findParentRegistry() as? BukkitComponentRegistry)?.findInstanceFactory(kParameter)
            ?: return null
        return factory.createInstance(plugin, kParameter, qualifier, scopeQualifier)
    }

    private fun findInstanceFactory(kParameter: KParameter): HQInstanceFactory<*>? {
        return registeredInstanceFactories.entries
            .firstOrNull { (type, _) -> type.starProjectedType.classifier == kParameter.type.classifier }
            ?.value
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

    override fun <T> unregisterInstanceFactory(instanceFactory: HQInstanceFactory<T>) {
        registeredInstanceFactories.values.removeIf { it === instanceFactory }
    }
}