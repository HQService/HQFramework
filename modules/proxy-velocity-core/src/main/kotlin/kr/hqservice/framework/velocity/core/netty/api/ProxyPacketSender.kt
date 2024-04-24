package kr.hqservice.framework.velocity.core.netty.api

import com.velocitypowered.api.proxy.ProxyServer
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Packet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.logging.Logger

@Bean
class ProxyPacketSender(
    private val proxy: ProxyServer,
    private val logger: Logger,
    private val channelContainer: NettyChannelRegistry
) : PacketSender {
    override fun sendPacketToProxy(packet: Packet) {
        throw UnsupportedOperationException("proxy to proxy")
    }

    override fun sendPacketAll(packet: Packet) {
        channelContainer.loopChannels {
            it.sendPacket(packet)
        }
    }

    override fun sendPacket(port: Int, packet: Packet) {
        channelContainer.getChannelByPort(port).sendPacket(packet)
    }

    override fun sendPacket(name: String, packet: Packet) {
        channelContainer.getChannelByServerName(name).sendPacket(packet)
    }

    override fun broadcast(message: String, logging: Boolean) {
        proxy.sendMessage(Component.text(message))
        if (logging) logger.info("[BROADCAST] $message")
    }

    override fun broadcast(message: Component, logging: Boolean) {

        proxy.sendMessage(message)
        if(logging) logger.info("[BROADCAST] ${LegacyComponentSerializer.legacySection().serialize(message)}")
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean) {
        val server = proxy.getServer(channel.getName()) ?: return
        server.get().playersConnected.forEach { it.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message)) }
        if (logging) logger.info("[BROADCAST_${channel.getName().uppercase()}] $message")
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: Component, logging: Boolean) {

        val server = proxy.getServer(channel.getName()) ?: return
        server.get().playersConnected.forEach { it.sendMessage(message) }
        if(logging) logger.info("[BROADCAST_${LegacyComponentSerializer.legacySection().serialize(message)}")
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean) {
        players.forEach { proxy.getPlayer(it.getUniqueId())?.run { if(isPresent) this else null }?.get()?.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message)) }
        if (logging) logger.info("[MESSAGE] $message")
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean) {
        sendMessageToPlayers(listOf(player), message, logging)
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: Component, logging: Boolean) {

        players.forEach { proxy.getPlayer(it.getUniqueId())?.run { if(isPresent) this else null }?.get()?.sendMessage(message) }
        if(logging) logger.info("[MESSAGE] ${LegacyComponentSerializer.legacySection().serialize(message)}")
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: Component, logging: Boolean) {
        sendMessageToPlayers(listOf(player), message, logging)
    }
}