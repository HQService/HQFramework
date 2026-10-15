package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.netty.HQNettyBootstrap
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import kr.hqservice.framework.proxy.core.netty.ProxyDefaultListeners
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.koin.core.component.KoinComponent
import java.util.logging.Logger

@Component
@Singleton(binds = [NettyServerBootstrap::class])
class NettyServerBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val channelRegistry: NettyChannelRegistry,
    private val packetSender: PacketSender
) : KoinComponent, HQSimpleComponent {

    private var bootstrap: HQNettyBootstrap? = null

    fun initializing() {
        val bootstrap = HQNettyBootstrap(logger, config).also { this.bootstrap = it }
        val future = bootstrap.initServer()
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
        bootstrap?.shutdown()
        bootstrap = null
    }

    private fun registerDefaultListeners() {
        ProxyDefaultListeners.registerCoreListeners(channelRegistry, logger)

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