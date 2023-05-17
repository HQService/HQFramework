package kr.hqservice.framework.netty

import kr.hqservice.framework.netty.channel.PacketCallbackHandler
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.PacketHandler
import kotlin.reflect.KClass

interface HQNettyService {
    fun broadcastPacket(packet: AbstractPacket)

    fun sendPacket(channelPort: Int, packet: AbstractPacket)

    fun sendPacket(channelName: String, packet: AbstractPacket)

    fun <T : AbstractPacket> registerOuterPacket(packetClass: KClass<T>)

    fun <T : AbstractPacket> registerInnerPacket(packetClass: KClass<T>, handler: PacketHandler<T>)

    fun <T : AbstractPacket> startCallback(packet: AbstractPacket, packetClass: KClass<T>, onReceived: PacketCallbackHandler<T>)
}