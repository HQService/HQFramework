package kr.hqservice.framework.bukkit.core.netty.server

import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.NettyServer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import org.bukkit.Server
import java.util.*
import kotlin.reflect.KClass

class LocalNettyServer(
    private val server: Server
) : NettyServer {
    override fun getChannels(): List<NettyChannel> {
        return listOf(NettyChannelImpl(server.port, server.name))
    }

    override fun getChannel(name: String): NettyChannel? {
        return getChannels().firstOrNull { it.getName() == name }
    }

    override fun getChannel(port: Int): NettyChannel? {
        return getChannels().firstOrNull { it.getPort() == port }
    }

    override fun getPlayer(name: String): NettyPlayer? {
        return getPlayers().firstOrNull { it.getName() == name }
    }

    override fun getPlayer(uniqueId: UUID): NettyPlayer? {
        return server.getPlayer(uniqueId)?.let { NettyPlayerImpl(it.name, it.uniqueId, NettyChannelImpl(server.port, server.name)) }
    }

    override fun getPlayers(): List<NettyPlayer> {
        return server.onlinePlayers.map { NettyPlayerImpl(it.name, it.uniqueId, NettyChannelImpl(server.port, server.name)) }
    }

    override fun getPlayers(channel: NettyChannel): List<NettyPlayer> {
        return if (channel.getName() == server.name && channel.getPort() == server.port) {
            this.getPlayers()
        } else {
            emptyList()
        }
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