package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Packet
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
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
        proxy.broadcast(message)
        if (logging) logger.info("[BROADCAST_ALL] $message")
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean) {
        val server = proxy.getServerInfo(channel.getName()) ?: return
        server.players.forEach { it.sendMessage(message) }
        if (logging) logger.info("[BROADCAST_${channel.getName().uppercase()}] $message")
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean) {
        players.forEach { proxy.getPlayer(it.getUniqueId())?.sendMessage(message) }
        if (logging) logger.info("[MESSAGE] ${ChatColor.stripColor(message)}")
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean) {
        sendMessageToPlayers(listOf(player), message, logging)
    }
}