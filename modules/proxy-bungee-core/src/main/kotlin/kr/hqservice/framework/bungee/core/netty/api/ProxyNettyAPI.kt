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
import java.util.UUID
import kotlin.reflect.KClass

@Component
@HQSingleton(binds = [HQNettyAPI::class])
class ProxyNettyAPI(
    private val packetSender: PacketSender,
) : HQNettyAPI, HQService {
    override fun getPacketSender(): PacketSender {
        return packetSender
    }

    override fun getChannels(): List<NettyChannel> {
        return ProxyServer.getInstance().servers.map { NettyChannelImpl(it.value.address.port, it.key) }
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