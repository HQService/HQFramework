package kr.hqservice.framework.proxy.core.netty

import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayPolicy
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.RelayingResult
import kr.hqservice.framework.netty.pipeline.TimeOutHandler
import kr.hqservice.framework.proxy.core.netty.registry.NettyChannelRegistry
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object ProxyDefaultListeners {
    fun registerCoreListeners(channelRegistry: NettyChannelRegistry, logger: Logger) {
        Direction.INBOUND.addListener(HandShakePacket::class) { packet, wrapper ->
            wrapper.port = packet.port
            wrapper.channel.writeAndFlush(HandShakePacket(-1))
            channelRegistry.registerActiveChannel(packet.port, wrapper)
            logger.info("registered channel ${channelRegistry.getChannelNameByPort(packet.port)}")
            Heartbeat(wrapper).start()
            wrapper.channel.pipeline().addFirst("timeout-handler", TimeOutHandler(5L, TimeUnit.SECONDS))
        }

        Direction.INBOUND.addListener(RelayingPacket::class) { packet, wrapper ->
            if (!RelayPolicy.isRelayable(packet.getRelayByte())) {
                logger.warning("dropped relay of a server-only packet from port ${wrapper.port}")
                return@addListener
            }
            try {
                try {
                    val port = packet.targetServer.toInt()
                    if (port == -1) {
                        channelRegistry.forEachChannels {
                            it.channel.writeAndFlush(RelayingResult(packet.getRelayByte()))
                        }
                        return@addListener
                    } else channelRegistry.getChannelByPort(port)
                } catch (e: NumberFormatException) {
                    channelRegistry.getChannelByServerName(packet.targetServer)
                }.channel.writeAndFlush(RelayingResult(packet.getRelayByte()))
            } catch (e: IllegalArgumentException) {
                logger.severe("Relaying packet failed due to TargetServer Offline!")
            }
        }
    }
}
