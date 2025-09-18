package kr.hqservice.framework.nms.v21_7

import io.papermc.paper.configuration.GlobalConfiguration
import io.papermc.paper.network.ChannelInitializeListenerHolder
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.nms.NMSServiceManager
import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.EarlyPacketHandler
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.v21_7.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import net.kyori.adventure.key.Key
import net.minecraft.core.UUIDUtil
import net.minecraft.network.protocol.login.ServerboundHelloPacket
import org.bukkit.plugin.Plugin

@Component
class NMSServiceManagerV21_7(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,
) : NMSServiceManager {
    private lateinit var serviceProvider: NMSServiceProviderImplV21_7
    private lateinit var virtualFactoryProvider: VirtualFactoryProviderImplV21_7

    override fun support(version: Version): Boolean {
        return version.ordinal in Version.V_21_7.ordinal .. Version.V_21_8.ordinal
    }

    override fun initialize() {
        ChannelInitializeListenerHolder.addListener(Key.key("hqservice:early-pipeline-hook")) { ch ->
            val pipeline = ch.pipeline()
            if (pipeline.get("hq_packet_handler") == null) {
                pipeline.addBefore(
                    "packet_handler", "hq_packet_handler",
                    EarlyPacketHandler(plugin, virtualHandlerRegistry) {
                        if (it is ServerboundHelloPacket) {
                            if (plugin.server.onlineMode || GlobalConfiguration.get().proxies.velocity.enabled) it.profileId
                            else UUIDUtil.createOfflinePlayerUUID(it.name)
                        } else null
                    }
                )
            }
        }

        val reflectionWrapper = NmsReflectionWrapperImpl()
        serviceProvider = NMSServiceProviderImplV21_7(plugin, languageRegistry, virtualHandlerRegistry, reflectionWrapper)
        virtualFactoryProvider = VirtualFactoryProviderImplV21_7(reflectionWrapper, serviceProvider)
    }

    override fun getServiceProvider(): NMSServiceProvider {
        return serviceProvider
    }

    override fun getVirtualFactoryProvider(): NMSVirtualFactoryProvider {
        return virtualFactoryProvider
    }
}