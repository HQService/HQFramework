package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import java.util.*
import kotlin.reflect.KClass

interface NettyServer {
    fun getChannels(): List<NettyChannel>

    fun getChannel(name: String): NettyChannel?

    fun getChannel(port: Int): NettyChannel?

    fun getPlayer(name: String): NettyPlayer?

    fun getPlayer(uniqueId: UUID): NettyPlayer?

    fun getPlayers(): List<NettyPlayer>

    fun getPlayers(channel: NettyChannel): List<NettyPlayer>

    fun <T : Packet> registerOuterPacket(packetClass: KClass<T>)

    fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: (packet: T, channel: ChannelWrapper) -> Unit,
    )

    fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: PacketHandler<T>,
    )
}