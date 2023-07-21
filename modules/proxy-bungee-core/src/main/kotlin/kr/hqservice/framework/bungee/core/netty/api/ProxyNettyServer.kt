package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.Singleton
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
import java.util.*
import kotlin.reflect.KClass

@Component
@Singleton(binds = [NettyServer::class])
class ProxyNettyServer(
    private val proxy: ProxyServer
) : NettyServer, HQService {

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
        return proxy.players.map { NettyPlayerImpl(it.name, it.uniqueId, getChannel(it.server.address.port)) }
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