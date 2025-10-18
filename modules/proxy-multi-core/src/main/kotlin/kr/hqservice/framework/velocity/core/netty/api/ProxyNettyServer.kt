package kr.hqservice.framework.velocity.core.netty.api

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
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
class ProxyNettyServer(
    private val proxy: ProxyServer,
) : NettyServer {
    private val redis = RedisBungeeAPI.getRedisBungeeApi()
    private val absRedis = RedisBungeeAPI.getAbstractRedisBungeeAPI()

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
        return redis.serverToPlayers.asMap().map { entry ->
            entry.value.map {
                val playerName = absRedis.getNameFromUuid(it)
                NettyPlayerImpl(playerName, playerName, UUID.randomUUID(), getChannel(entry.key))
            }
        }.flatten()
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