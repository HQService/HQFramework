package kr.hqservice.framework.nms.legacy

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.nms.NMSServiceManager
import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.reflect.LegacyNmsReflectionWrapperImpl
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import org.bukkit.plugin.Plugin

@Component
class LegacyNMSServiceManager(
    private val plugin: Plugin,
    private val server: Server,
    private val languageRegistry: LanguageRegistry,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,
    private val config: HQYamlConfiguration
) : NMSServiceManager {
    private lateinit var serviceProvider: LegacyNMSServiceProvider
    private lateinit var virtualFactoryProvider: LegacyVirtualFactoryProvider

    override fun support(version: Version): Boolean {
        return version.ordinal <= Version.V_20_4.ordinal
    }

    override fun initialize() {
        val reflectionWrapper = LegacyNmsReflectionWrapperImpl(server, config)
        serviceProvider = LegacyNMSServiceProvider(plugin, languageRegistry, virtualHandlerRegistry, reflectionWrapper)
        virtualFactoryProvider = LegacyVirtualFactoryProvider(reflectionWrapper, serviceProvider)
    }

    override fun getServiceProvider(): NMSServiceProvider {
        return serviceProvider
    }

    override fun getVirtualFactoryProvider(): NMSVirtualFactoryProvider {
        return virtualFactoryProvider
    }
}