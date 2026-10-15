package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.NettyServer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import net.md_5.bungee.api.ProxyServer
import java.net.InetSocketAddress
import java.util.*
import kotlin.reflect.KClass

@Bean
class ProxyNettyServer(
    private val proxy: ProxyServer
) : NettyServer {

    override fun getChannels(): List<NettyChannel> {
        return proxy.servers.map { NettyChannelImpl(it.value.address.port, it.key) }
    }

    override fun getChannel(name: String): NettyChannel? {
        return getChannels().firstOrNull { it.getName() == name }
    }

    override fun getChannel(port: Int): NettyChannel? {
        return getChannels().firstOrNull { it.getPort() == port }
    }

    override fun getPlayers(): List<NettyPlayer> {
        return proxy.players.mapNotNull { player ->
            val serverAddress = player.server?.info?.socketAddress as? InetSocketAddress ?: return@mapNotNull null
            NettyPlayerImpl(player.name, player.displayName, player.uniqueId, getChannel(serverAddress.port))
        }
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