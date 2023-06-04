package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import kr.hqservice.framework.netty.container.ChannelContainer
import java.util.UUID
import kotlin.reflect.KClass

interface HQNettyAPI {
    fun getPacketSender(): PacketSender

    fun getChannels(): List<NettyChannel>

    fun broadcast(message: String, logging: Boolean = true)

    fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean = true)

    fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean = true)

    fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean = true)

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