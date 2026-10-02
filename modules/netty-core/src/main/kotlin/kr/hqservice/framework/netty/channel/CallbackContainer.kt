package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.Packet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

class CallbackContainer(private val logger: Logger) {
    private val callbackMap = ConcurrentHashMap<KClass<out Packet>, ConcurrentLinkedQueue<PacketCallbackHandler<out Packet>>>()

    fun addOnQueue(
        channel: ChannelWrapper,
        packet: Packet,
        targetClass: KClass<out Packet>,
        callback: PacketCallbackHandler<out Packet>
    ) {
        val queue = callbackMap.computeIfAbsent(targetClass) { ConcurrentLinkedQueue() }
        synchronized(queue) {
            queue.add(callback)
            val future = channel.writePacket(packet)
            if (future == null) {
                queue.remove(callback)
                return
            }
            future.addListener {
                if (!it.isSuccess && queue.remove(callback)) {
                    logger.log(Level.WARNING, "dropped ${targetClass.simpleName} callback: sending ${packet::class.simpleName} failed", it.cause())
                }
            }
        }
    }

    @Suppress("unchecked_cast")
    fun complete(packet: Packet): Boolean {
        val queue = callbackMap[packet::class] ?: return false
        val callback = synchronized(queue) { queue.poll() } as? PacketCallbackHandler<Packet> ?: return false
        callback.onCallbackReceived(packet)
        return true
    }

}
