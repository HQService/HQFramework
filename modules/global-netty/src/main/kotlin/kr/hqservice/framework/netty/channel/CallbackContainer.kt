package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.AbstractPacket
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.reflect.KClass

class CallbackContainer {
    private val callbackMap = ConcurrentHashMap<KClass<out AbstractPacket>, Queue<PacketCallback<out AbstractPacket>>>()
    private val lock = ReentrantReadWriteLock()

    fun addOnQueue(channel: ChannelWrapper, packet: AbstractPacket, targetClass: KClass<out AbstractPacket>, callback: PacketCallback<out AbstractPacket>) {
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
    fun complete(packet: AbstractPacket): Boolean {
        try {
            lock.readLock().lock()
            val queue = callbackMap[packet::class]
            return if (!queue.isNullOrEmpty()) {
                val callback : PacketCallback<AbstractPacket> = queue.poll() as? PacketCallback<AbstractPacket> ?: return false
                callback.onCallbackReceived(packet)
                true
            } else false
        } finally {
            lock.readLock().unlock()
        }
    }

}