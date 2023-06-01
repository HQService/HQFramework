package kr.hqservice.framework.bungee.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.AbstractPacket
import kotlin.reflect.KClass

class NettyPacketReceivedEvent(
    val packet: AbstractPacket,
    val wrapper: ChannelWrapper
) : PacketEvent() {
    fun isPacketOf(clazz: KClass<out AbstractPacket>): Boolean {
        return clazz.isInstance(packet)
    }
}