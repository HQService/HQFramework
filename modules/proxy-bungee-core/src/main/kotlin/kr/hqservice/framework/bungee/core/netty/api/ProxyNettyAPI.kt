package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.netty.api.HQNettyAPI
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.PacketHandler
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import java.util.UUID
import java.util.logging.Logger
import kotlin.reflect.KClass

@Component
@HQSingleton(binds = [HQNettyAPI::class])
class ProxyNettyAPI(
    private val packetSender: PacketSender,
    private val logger: Logger
) : HQNettyAPI, HQService {
    private val proxy: ProxyServer by lazy { ProxyServer.getInstance() }

    override fun getPacketSender(): PacketSender {
        return packetSender
    }

    override fun getChannels(): List<NettyChannel> {
        return ProxyServer.getInstance().servers.map { NettyChannelImpl(it.value.address.port, it.key) }
    }

    override fun broadcast(message: String, logging: Boolean) {
        val msg = TextComponent(message)
        proxy.broadcast(msg)
        if(logging) logger.info("[BROADCAST_ALL] $message")
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean) {
        val msg = TextComponent(message)
        val server = proxy.getServerInfo(channel.getName())?: return
        server.players.forEach { it.sendMessage(msg) }
        if(logging) logger.info("[BROADCAST_${channel.getName().uppercase()}] $message")
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean) {
        val msg = TextComponent(message)
        players.forEach { proxy.getPlayer(it.getUniqueId())?.sendMessage(msg) }
        if(logging) logger.info("[MESSAGE] $message")
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean) {
        sendMessageToPlayers(listOf(player), message, logging)
    }

    override fun getChannel(name: String): NettyChannel? {
        return getChannels().firstOrNull { it.getName() == name }
    }

    override fun getChannel(port: Int): NettyChannel? {
        return getChannels().firstOrNull { it.getPort() == port }
    }

    override fun getPlayers(): List<NettyPlayer> {
        return ProxyServer.getInstance().players.map { NettyPlayerImpl(it.name, it.uniqueId, getChannel(it.server.address.port)) }
    }

    override fun getPlayers(channel: NettyChannel): List<NettyPlayer> {
        return getPlayers().filter { it.getChannel() == channel }
    }

    override fun getPlayer(name: String): NettyPlayer? {
        return getPlayers().firstOrNull { it.getName() == name }
    }

    override fun getPlayer(uniqueId: UUID): NettyPlayer? {
        return getPlayers().firstOrNull { it.getUniqueId() == uniqueId }
    }

    override fun <T : Packet> registerOuterPacket(packetClass: KClass<T>) {
        Direction.OUTBOUND.registerPacket(packetClass)
    }

    override fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: (packet: T, channel: ChannelWrapper) -> Unit,
    ) {
        Direction.INBOUND.registerPacket(packetClass)
        Direction.INBOUND.addListener(packetClass, packetHandler)
    }

    override fun <T : Packet> registerInnerPacket(packetClass: KClass<T>, packetHandler: PacketHandler<T>) {
        Direction.INBOUND.registerPacket(packetClass)
        Direction.INBOUND.addListener(packetClass, packetHandler)
    }
}