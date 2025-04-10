package kr.hqservice.framework.nms.v21_3

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.nms.NMSServiceManager
import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.plugin.Plugin

@Component
class NMSServiceManagerV21_3(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,
) : NMSServiceManager {
    private lateinit var serviceProvider: NMSServiceProviderV21_3
    private lateinit var virtualFactoryProvider: VirtualFactoryProviderV21_3

    override fun support(version: Version): Boolean {
        return version.ordinal in Version.V_21_3.ordinal .. Version.V_21_4.ordinal
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