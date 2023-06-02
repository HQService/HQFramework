package kr.hqservice.framework.netty

import kr.hqservice.framework.netty.channel.PacketCallbackHandler
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import kotlin.reflect.KClass

interface HQNettyService {
    fun broadcastPacket(packet: Packet)

    fun sendPacket(channelPort: Int, packet: Packet)

    fun sendPacket(channelName: String, packet: Packet)

    fun <T : Packet> registerOuterPacket(packetClass: KClass<T>)

    fun <T : Packet> registerInnerPacket(packetClass: KClass<T>, handler: PacketHandler<T>)

    fun <T : Packet> startCallback(packet: Packet, packetClass: KClass<T>, onReceived: PacketCallbackHandler<T>)
}