package kr.hqservice.framework.netty.packet

import kr.hqservice.framework.netty.channel.ChannelWrapper
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

enum class Direction {
    INBOUND,
    OUTBOUND;

    protected val packetMap = mutableMapOf<String, PacketWrapper<*>>()
    protected val handlers = mutableMapOf<KClass<out AbstractPacket>, LinkedList<(AbstractPacket, ChannelWrapper)-> Unit>>()

    fun <T: AbstractPacket> registerPacket(packetClass: KClass<T>, constructor: KFunction<T>) {

    }

}