package kr.hqservice.framework.bukkit.core.netty

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.bukkit.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.bukkit.core.netty.service.impl.HQNettyServiceImpl
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.yaml.extension.yaml
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import java.io.File
import java.util.logging.Logger

@Component
class NettyModule(
    private val plugin: Plugin,
    private val logger: Logger,
    private val channelHandler: ChannelMainHandler,
    private val nettyService: HQNettyService
) : HQModule, KoinComponent {
    private val nettyEnabled: Boolean get() = nettyService.isEnable()

    override fun onEnable() {
        val config = File(plugin.dataFolder, "config.yml").yaml()
        (nettyService as HQNettyServiceImpl).enabled = config.getBoolean("netty.enabled")
        //nettyEnabled = config.getBoolean("netty.enabled")

        channelHandler.apply {
            Direction.INBOUND.addListener(HandShakePacket::class, this) }

        Direction.INBOUND.addListener(ShutdownPacket::class) { packet, channel ->
            if(packet.shutdownTarget) {
                channel.setEnabled(false)
                nettyService.enabled = false
                logger.info("requested shutdown.")
                Bukkit.shutdown()
            } else {
                channel.setEnabled(false)
                logger.info("proxy executed shutdown procedure")
            }
        }

        if(nettyEnabled) NettyClientBootstrap(plugin, logger, config).initializing()
    }

    override fun onDisable() {
        channelHandler.disconnect()
    }
}