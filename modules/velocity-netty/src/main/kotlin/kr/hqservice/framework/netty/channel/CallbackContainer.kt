package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.Packet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.reflect.KClass

class CallbackContainer {
    private val callbackMap = ConcurrentHashMap<KClass<out Packet>, Queue<PacketCallbackHandler<out Packet>>>()
    private val lock = ReentrantReadWriteLock()

    fun addOnQueue(
        channel: ChannelWrapper,
        packet: Packet,
        targetClass: KClass<out Packet>,
        callback: PacketCallbackHandler<out Packet>
    ) {
        try {
            lock.writeLock().lock()
            channel.sendPacket(packet)
            callbackMap.computeIfAbsent(targetClass) {
                LinkedList()
            }.add(callback)
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Suppress("unchecked_cast")
    fun complete(packet: Packet): Boolean {
        try {
            lock.readLock().lock()
            val queue = callbackMap[packet::class]
            return if (!queue.isNullOrEmpty()) {
                val callback: PacketCallbackHandler<Packet> =
                    queue.poll() as? PacketCallbackHandler<Packet> ?: return false
                callback.onCallbackReceived(packet)
                true
            } else false
        } finally {
            lock.readLock().unlock()
        }
    }

}