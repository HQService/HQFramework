package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.netty.HQNettyBootstrap
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.RelayingResult
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.framework.netty.pipeline.TimeOutHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

@Component
@HQSingleton(binds = [NettyServerBootstrap::class])
class NettyServerBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val channelRegistry: NettyChannelRegistry,
    private val packetSender: PacketSender
) : KoinComponent, HQSimpleComponent {

    fun initializing() {
        val future = HQNettyBootstrap(logger, config).initServer()
        future.whenCompleteAsync { _, throwable ->
            if (throwable != null) {
                logger.severe("failed to bootup successfully.")
                throwable.printStackTrace()
            } else logger.info("server initialization success!")
        }

        Direction.INBOUND.registerPacket(BroadcastPacket::class)
        Direction.INBOUND.registerPacket(MessagePacket::class)
        registerDefaultListeners()
    }

    fun shutdown() {
        channelRegistry.shutdown()
    }

    private fun registerDefaultListeners() {
        Direction.INBOUND.addListener(HandShakePacket::class) { packet, wrapper ->
            wrapper.port = packet.port
            wrapper.handler.connectionState = ConnectionState.CONNECTED
            wrapper.channel.writeAndFlush(HandShakePacket(-1))
            channelRegistry.registerActiveChannel(packet.port, wrapper)
            logger.info("registered channel ${channelRegistry.getChannelNameByPort(packet.port)}")
            PingPongManagementThread(wrapper).start()
            wrapper.channel.pipeline().addFirst("timeout-handler", TimeOutHandler(5L, TimeUnit.SECONDS))
        }

        Direction.INBOUND.addListener(RelayingPacket::class) { packet, _ ->
            try {
                try {
                    val port = packet.targetServer.toInt()
                    if (port == -1) {
                        channelRegistry.forEachChannels {
                            //it.sendPacket(RelayingResult(packet.getRelay()))
                            it.channel.writeAndFlush(RelayingResult(packet.getRelayByte()))
                        }
                        return@addListener
                    } else channelRegistry.getChannelByPort(port)
                } catch (e: NumberFormatException) {
                    channelRegistry.getChannelByServerName(packet.targetServer)
                }.channel.writeAndFlush(RelayingResult(packet.getRelayByte()))
                /*sendPacket(RelayingResult(packet.getRelay()))*/
            } catch (e: IllegalArgumentException) {
                logger.severe("Relaying packet failed due to TargetServer Offline!")
            }
        }

        Direction.INBOUND.addListener(BroadcastPacket::class) { packet, _ ->
            val targetChannel = packet.targetChannel
            if (targetChannel != null) {
                packetSender.sendMessageToChannel(targetChannel, packet.message, packet.logging)
            } else packetSender.broadcast(packet.message, packet.logging)
        }

        Direction.INBOUND.addListener(MessagePacket::class) { packet, _ ->
            packetSender.sendMessageToPlayers(packet.receivers, packet.message, packet.logging)
        }
    }
}