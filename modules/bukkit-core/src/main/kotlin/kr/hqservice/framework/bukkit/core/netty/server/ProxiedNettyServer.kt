package kr.hqservice.framework.bukkit.core.netty.server

import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.NettyServer
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.container.ChannelContainer
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import java.util.*
import kotlin.reflect.KClass

class ProxiedNettyServer(
    private val container: ChannelContainer
) : NettyServer {
    override fun getChannels(): List<NettyChannel> {
        return container.getChannels()
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
        return getPlayers().firstOrNull { it.getUniqueId() == uniqueId }
    }

    override fun getPlayers(): List<NettyPlayer> {
        return container.getPlayers()
    }

    override fun getPlayers(channel: NettyChannel): List<NettyPlayer> {
        return container.getPlayers(channel)
    }

    @Deprecated("@PacketListener(outbound = [...]) 를 쓰거나 첫 전송 때의 자동 등록에 맡기세요")

    override fun <T : Packet> registerOuterPacket(packetClass: KClass<T>) {
        Direction.OUTBOUND.registerPacket(packetClass)
    }

    @Deprecated("@PacketListener 클래스의 @PacketSubscribe 함수로 받으세요")

    override fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: (packet: T, channel: ChannelWrapper) -> Unit,
    ) {
        Direction.INBOUND.registerPacket(packetClass)
        Direction.INBOUND.addListener(packetClass, packetHandler)
    }

    @Deprecated("@PacketListener 클래스의 @PacketSubscribe 함수로 받으세요")

    override fun <T : Packet> registerInnerPacket(packetClass: KClass<T>, packetHandler: PacketHandler<T>) {
        Direction.INBOUND.registerPacket(packetClass)
        Direction.INBOUND.addListener(packetClass, packetHandler)
    }
}