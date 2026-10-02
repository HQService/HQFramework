package kr.hqservice.framework.database.redis

import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class LettucePubSubTransport(private val provider: RedisProvider) : PubSubTransport {
    private val listeners = ConcurrentHashMap<String, CopyOnWriteArrayList<(ByteArray) -> Unit>>()
    private val connection: StatefulRedisPubSubConnection<String, ByteArray> by lazy {
        provider.pubSubConnection().also { it.addListener(Dispatcher()) }
    }

    override fun publish(channel: String, payload: ByteArray) {
        provider.connection().async().publish(channel, payload)
    }

    override fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable {
        synchronized(listeners) {
            val channelListeners = listeners.getOrPut(channel) { CopyOnWriteArrayList() }
            channelListeners.add(listener)
            if (channelListeners.size == 1) connection.sync().subscribe(channel)
        }
        return AutoCloseable { unsubscribe(channel, listener) }
    }

    private fun unsubscribe(channel: String, listener: (ByteArray) -> Unit) {
        synchronized(listeners) {
            val channelListeners = listeners[channel] ?: return
            if (!channelListeners.remove(listener) || channelListeners.isNotEmpty()) return
            listeners.remove(channel)
            connection.sync().unsubscribe(channel)
        }
    }

    private inner class Dispatcher : RedisPubSubAdapter<String, ByteArray>() {
        override fun message(channel: String, message: ByteArray) {
            listeners[channel]?.forEach { it(message) }
        }
    }
}
