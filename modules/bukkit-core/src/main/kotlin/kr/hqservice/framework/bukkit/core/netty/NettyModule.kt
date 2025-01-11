package kr.hqservice.framework.bukkit.core.netty

import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.bukkit.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.bukkit.core.netty.service.impl.HQNettyServiceImpl
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

@Module
class NettyModule(
    private val plugin: Plugin,
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val channelHandler: ChannelMainHandler,
    private val nettyService: HQNettyService
) {
    private val nettyEnabled: Boolean get() = nettyService.isEnable()

    @Setup
    fun setup() {
        (nettyService as HQNettyServiceImpl).enabled = config.getBoolean("netty.enabled")

        Direction.INBOUND.addListener(HandShakePacket::class, channelHandler)
        Direction.INBOUND.addListener(ShutdownPacket::class) { packet, channel ->
            if (packet.shutdownTarget) {
                channel.setEnabled(false)
                nettyService.enabled = false
                logger.info("requested shutdown.")
                Bukkit.shutdown()
            } else {
                channel.setEnabled(false)
                logger.info("proxy executed shutdown procedure")
            }
        }

        if (nettyEnabled) {
            NettyClientBootstrap(plugin, logger, config).initializing()
        }
    }

    @Teardown
    fun teardown() {
        channelHandler.disconnect()
    }
}