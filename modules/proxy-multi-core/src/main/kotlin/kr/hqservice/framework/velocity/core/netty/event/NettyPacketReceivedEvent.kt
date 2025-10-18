package kr.hqservice.framework.velocity.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kotlin.reflect.KClass

class NettyPacketReceivedEvent(
    val packet: Packet,
    val wrapper: ChannelWrapper
) : PacketEvent() {
    fun isPacketOf(clazz: KClass<out Packet>): Boolean {
        return clazz.isInstance(packet)
    }
}