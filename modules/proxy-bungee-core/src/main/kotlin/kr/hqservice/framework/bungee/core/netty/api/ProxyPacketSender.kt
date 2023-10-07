package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Packet
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
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
        if (logging) logger.info("[BROADCAST] $message")
    }

    override fun broadcast(message: BaseComponent, logging: Boolean) {
        val newComponent = legacyToNewComponentStyle(message)

        proxy.broadcast(newComponent)
        if(logging) logger.info("[BROADCAST] ${ChatColor.stripColor(message.toLegacyText())}")
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean) {
        val server = proxy.getServerInfo(channel.getName()) ?: return
        server.players.forEach { it.sendMessage(message) }
        if (logging) logger.info("[BROADCAST_${channel.getName().uppercase()}] $message")
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: BaseComponent, logging: Boolean) {
        val newComponent = legacyToNewComponentStyle(message)

        proxy.broadcast(newComponent)
        if(logging) logger.info("[BROADCAST_${channel.getName().uppercase()}] ${ChatColor.stripColor(message.toLegacyText())}")
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean) {
        players.forEach { proxy.getPlayer(it.getUniqueId())?.sendMessage(message) }
        if (logging) logger.info("[MESSAGE] ${ChatColor.stripColor(message)}")
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean) {
        sendMessageToPlayers(listOf(player), message, logging)
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: BaseComponent, logging: Boolean) {
        val newComponent = legacyToNewComponentStyle(message)

        players.forEach { proxy.getPlayer(it.getUniqueId())?.sendMessage(newComponent) }
        if(logging) logger.info("[MESSAGE] ${ChatColor.stripColor(message.toLegacyText())}")
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: BaseComponent, logging: Boolean) {
        sendMessageToPlayers(listOf(player), message, logging)
    }

    private fun legacyToNewComponentStyle(legacyComponent: BaseComponent): BaseComponent {
        val extra = legacyComponent.extra
        val newComponent = TextComponent()
        newComponent.hoverEvent = legacyComponent.hoverEvent
        newComponent.clickEvent = legacyComponent.clickEvent

        if (extra != null && extra.isNotEmpty()) {
            extra.forEach {
                val child = TextComponent()
                val legacy = it.toLegacyText()
                TextComponent.fromLegacyText(legacy).forEach { newText ->
                    child.addExtra(newText)
                }
                child.hoverEvent = it.hoverEvent
                child.clickEvent = it.clickEvent
                newComponent.addExtra(child)
            }
        } else {
            val child = TextComponent()
            val legacy = legacyComponent.toLegacyText()
            TextComponent.fromLegacyText(legacy).forEach { newText ->
                child.addExtra(newText)
            }
            newComponent.addExtra(child)
        }

        return newComponent
    }
}