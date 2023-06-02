package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import kotlin.reflect.KClass

interface HQNettyAPI {
    fun getPacketSender(): PacketSender

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