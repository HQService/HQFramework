package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.netty.HQServerBootstrap
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.RelayingResult
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.framework.netty.pipeline.TimeOutHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.lang.NumberFormatException
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class NettyServerBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration
) {
    private val channelContainer = NettyChannelContainer(config.getBoolean("" + "netty.shutdown-servers"))

    fun initializing() {
        val future = HQServerBootstrap(logger, config).initServer()
        future.whenCompleteAsync { _, throwable ->
            if(throwable != null) {
                logger.severe("failed to bootup successfully.")
                throwable.printStackTrace()
            } else logger.info("server initialization success!")
        }
        registerDefaultListeners()
    }

    fun shutdown() { channelContainer.shutdown() }

    private fun registerDefaultListeners() {
        Direction.INBOUND.addListener(HandShakePacket::class) { packet, wrapper ->
            wrapper.port = packet.port
            wrapper.handler.setConnectionState(ConnectionState.CONNECTED)
            wrapper.channel.writeAndFlush(HandShakePacket(-1))
            channelContainer.onChannelActive(packet.port, wrapper)
            logger.info("registered channel ${channelContainer.getChannelNameByPort(packet.port)}")
            PingPongManagementThread(wrapper).start()
            wrapper.channel.pipeline().addFirst("timeout-handler", TimeOutHandler(5L, TimeUnit.SECONDS))
        }

        Direction.INBOUND.addListener(RelayingPacket::class) { packet, _ ->
            try {
                try {
                    val port = packet.getTargetServer().toInt()
                    if (port == -1) {
                        channelContainer.forEachChannels { it.sendPacket(RelayingResult(packet.getRelay())) }
                        return@addListener
                    } else channelContainer.getChannelByPort(port)
                } catch (e: NumberFormatException) {
                    channelContainer.getChannelByServerName(packet.getTargetServer())
                }.sendPacket(RelayingResult(packet.getRelay()))
            } catch (e: IllegalArgumentException) {
                logger.severe("Relaying packet failed due to TargetServer Offline!")
            }
        }
    }
}