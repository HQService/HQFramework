package kr.hqservice.framework.core.netty

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.yaml.extension.yaml
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import java.io.File
import java.util.logging.Logger

@Component
class NettyModule(
    private val plugin: Plugin,
    private val logger: Logger,
    private val channelHandler: ChannelMainHandler
) : HQModule, KoinComponent {
    private var nettyEnabled: Boolean = false

    override fun onEnable() {
        val config = File(plugin.dataFolder, "config.yml").yaml()
        nettyEnabled = config.getBoolean("netty.enabled")

        channelHandler.apply {
            Direction.INBOUND.addListener(HandShakePacket::class, this) }

        Direction.INBOUND.addListener(ShutdownPacket::class) { packet, channel ->
            if(packet.shutdownTarget) {
                channel.setEnabled(false)
                nettyEnabled = false
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