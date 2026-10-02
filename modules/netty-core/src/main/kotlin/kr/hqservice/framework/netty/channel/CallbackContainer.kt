package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.Packet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

class CallbackContainer {
    private val callbackMap = ConcurrentHashMap<KClass<out Packet>, ConcurrentLinkedQueue<PacketCallbackHandler<out Packet>>>()

    fun addOnQueue(
        channel: ChannelWrapper,
        packet: Packet,
        targetClass: KClass<out Packet>,
        callback: PacketCallbackHandler<out Packet>
    ) {
        val queue = callbackMap.computeIfAbsent(targetClass) { ConcurrentLinkedQueue() }
        queue.add(callback)
        if (!channel.sendPacket(packet)) queue.remove(callback)
    }

    @Suppress("unchecked_cast")
    fun complete(packet: Packet): Boolean {
        val callback = callbackMap[packet::class]?.poll() as? PacketCallbackHandler<Packet> ?: return false
        callback.onCallbackReceived(packet)
        return true
    }

}
