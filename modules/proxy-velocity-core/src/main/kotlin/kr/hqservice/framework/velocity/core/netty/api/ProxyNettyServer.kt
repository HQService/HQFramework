package kr.hqservice.framework.velocity.core.netty.api

import com.velocitypowered.api.proxy.ProxyServer
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
import java.util.*
import kotlin.reflect.KClass

@Bean
open class ProxyNettyServer(
    private val proxy: ProxyServer
) : NettyServer {

    override fun getChannels(): List<NettyChannel> {
        return proxy.allServers.map { NettyChannelImpl(it.serverInfo.address.port, it.serverInfo.name) }
    }

    override fun getChannel(name: String): NettyChannel? {
        return getChannels().firstOrNull { it.getName() == name }
    }

    override fun getChannel(port: Int): NettyChannel? {
        return getChannels().firstOrNull { it.getPort() == port }
    }

    override fun getPlayers(): List<NettyPlayer> {
        return proxy.allPlayers.mapNotNull { player ->
            val server = player.currentServer.orElse(null) ?: return@mapNotNull null
            NettyPlayerImpl(player.username, player.username, player.uniqueId, getChannel(server.serverInfo.address.port))
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