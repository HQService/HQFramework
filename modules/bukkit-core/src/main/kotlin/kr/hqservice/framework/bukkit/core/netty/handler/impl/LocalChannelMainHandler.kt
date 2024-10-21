package kr.hqservice.framework.bukkit.core.netty.handler.impl

import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.bukkit.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import net.md_5.bungee.api.chat.BaseComponent

class LocalChannelMainHandler(
    private val plugin: HQBukkitPlugin
) : ChannelMainHandler {
    private var proxyChannel: ChannelWrapper? = null

    override fun onPacketReceive(packet: HandShakePacket, channel: ChannelWrapper) {
        channel.handler.connectionState = ConnectionState.CONNECTED
        this.proxyChannel = channel.handler.channel
        plugin.launch {
            plugin.server.pluginManager.callEvent(NettyClientConnectedEvent(channel))
        }
    }

    override fun sendPacketToProxy(packet: Packet) {
        proxyChannel?.sendPacket(packet)
    }

    override fun sendPacketAll(packet: Packet) {
        proxyChannel?.sendPacket(RelayingPacket(packet))
    }

    override fun sendPacket(port: Int, packet: Packet) {
        proxyChannel?.sendPacket(RelayingPacket(packet, port.toString()))
    }

    override fun sendPacket(name: String, packet: Packet) {
        proxyChannel?.sendPacket(RelayingPacket(packet, name))
    }

    override fun disconnect() {
        if (proxyChannel?.channel?.isOpen == true && proxyChannel?.channel?.isActive == true) {
            proxyChannel?.channel?.disconnect()
            proxyChannel = null
        }
    }

    override fun broadcast(message: String, logging: Boolean) {
        plugin.server.broadcastMessage(message)
    }

    override fun broadcast(message: BaseComponent, logging: Boolean) {
        plugin.server.spigot().broadcast(message)
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean) {
        if (plugin.server.port == channel.getPort() && plugin.server.name == channel.getName()) {
            plugin.server.broadcastMessage(message)
        }
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: BaseComponent, logging: Boolean) {
        if (plugin.server.port == channel.getPort() && plugin.server.name == channel.getName()) {
            plugin.server.spigot().broadcast(message)
        }
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean) {
        players.forEach { player ->
            plugin.server.getPlayer(player.getUniqueId())?.sendMessage(message)
        }
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean) {
        plugin.server.getPlayer(player.getUniqueId())?.sendMessage(message)
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: BaseComponent, logging: Boolean) {
        players.forEach { player ->
            plugin.server.getPlayer(player.getUniqueId())?.spigot()?.sendMessage(message)
        }
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: BaseComponent, logging: Boolean) {
        plugin.server.getPlayer(player.getUniqueId())?.spigot()?.sendMessage(message)
    }
}