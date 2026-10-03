package kr.hqservice.framework.database.redis

import java.util.concurrent.CopyOnWriteArrayList

class InMemoryPubSubTransport : PubSubTransport {
    val listeners = mutableMapOf<String, CopyOnWriteArrayList<(ByteArray) -> Unit>>()

    fun listenerCount(channel: String): Int = listeners[channel]?.size ?: 0

    override fun publish(channel: String, payload: ByteArray) {
        listeners[channel]?.forEach { it(payload) }
    }

    override fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable {
        listeners.getOrPut(channel) { CopyOnWriteArrayList() }.add(listener)
        return AutoCloseable { listeners[channel]?.remove(listener) }
    }
}
