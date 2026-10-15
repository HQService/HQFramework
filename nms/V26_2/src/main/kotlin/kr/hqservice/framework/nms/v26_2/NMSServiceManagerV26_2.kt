package kr.hqservice.framework.nms.v26_2

import io.papermc.paper.configuration.GlobalConfiguration
import io.papermc.paper.network.ChannelInitializeListenerHolder
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.nms.NMSServiceManager
import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.hook.EarlyHookInstaller
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.v21_11.VirtualFactoryProviderImpl
import kr.hqservice.framework.nms.v21_11.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import net.minecraft.core.UUIDUtil
import net.minecraft.network.protocol.login.ServerboundHelloPacket
import org.bukkit.plugin.Plugin

@Component
class NMSServiceManagerV26_2(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,
) : NMSServiceManager {
    private lateinit var serviceProvider: NMSServiceProviderV26_2
    private lateinit var virtualFactoryProvider: VirtualFactoryProviderImpl

    override fun support(version: Version): Boolean {
        return version.ordinal == Version.V_26_2.ordinal
    }

    override fun initialize() {
        EarlyHookInstaller.install(
            plugin, virtualHandlerRegistry,
            { key, initializer -> ChannelInitializeListenerHolder.addListener(key) { initializer(it) } },
            { key -> ChannelInitializeListenerHolder.removeListener(key) }
        ) {
            if (it is ServerboundHelloPacket) {
                if (plugin.server.onlineMode || GlobalConfiguration.get().proxies.velocity.enabled) it.profileId
                else UUIDUtil.createOfflinePlayerUUID(it.name)
            } else null
        }

        val reflectionWrapper = NmsReflectionWrapperImpl()
        serviceProvider = NMSServiceProviderV26_2(plugin, languageRegistry, virtualHandlerRegistry, reflectionWrapper)
        virtualFactoryProvider = VirtualFactoryProviderImpl(reflectionWrapper, serviceProvider)
    }

    override fun getServiceProvider(): NMSServiceProvider {
        return serviceProvider
    }

    override fun getVirtualFactoryProvider(): NMSVirtualFactoryProvider {
        return virtualFactoryProvider
    }
}