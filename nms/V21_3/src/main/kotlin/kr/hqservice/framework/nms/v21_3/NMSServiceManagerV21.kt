package kr.hqservice.framework.nms.v21_3

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.nms.NMSServiceManager
import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import org.bukkit.plugin.Plugin

@Component
class NMSServiceManagerV21(
    private val plugin: Plugin,
    private val server: Server,
    private val languageRegistry: LanguageRegistry,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,
    private val config: HQYamlConfiguration
) : NMSServiceManager {
    private lateinit var serviceProvider: NMSServiceProviderV21_3
    private lateinit var virtualFactoryProvider: VirtualFactoryProviderV21_3

    override fun support(version: Version): Boolean {
        return version.ordinal == Version.V_21_3.ordinal
    }

    override fun initialize() {
        val reflectionWrapper = NmsReflectionWrapperImpl()
        serviceProvider = NMSServiceProviderV21_3(plugin, languageRegistry, virtualHandlerRegistry, reflectionWrapper)
        virtualFactoryProvider = VirtualFactoryProviderV21_3(reflectionWrapper, serviceProvider)
    }

    override fun getServiceProvider(): NMSServiceProvider {
        return serviceProvider
    }

    override fun getVirtualFactoryProvider(): NMSVirtualFactoryProvider {
        return virtualFactoryProvider
    }
}