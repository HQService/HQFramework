package kr.hqservice.framework.database.redis

import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.logging.Level
import java.util.logging.Logger

class LettucePubSubTransport(private val provider: RedisProvider, private val logger: Logger) : PubSubTransport {
    private val listeners = ConcurrentHashMap<String, CopyOnWriteArrayList<(ByteArray) -> Unit>>()
    private val subscribed = ConcurrentHashMap.newKeySet<String>()
    private val executor = Executors.newSingleThreadExecutor { Thread(it, "hq-redis-pubsub").apply { isDaemon = true } }
    private val connection: StatefulRedisPubSubConnection<String, ByteArray> by lazy {
        provider.pubSubConnection().also { it.addListener(Dispatcher()) }
    }

    override fun publish(channel: String, payload: ByteArray) {
        provider.connection().async().publish(channel, payload)
    }

    override fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable {
        synchronized(listeners) {
            if (subscribed.add(channel)) {
                try {
                    connection.async().subscribe(channel).whenComplete { _, error ->
                        if (error != null) {
                            subscribed.remove(channel)
                            logger.log(Level.WARNING, "failed to subscribe to redis channel $channel; the next subscription retries", error)
                        }
                    }
                } catch (e: Exception) {
                    subscribed.remove(channel)
                    throw e
                }
            }
            listeners.getOrPut(channel) { CopyOnWriteArrayList() }.add(listener)
        }
        return AutoCloseable { unsubscribe(channel, listener) }
    }

    override fun close() {
        executor.shutdown()
    }

    private fun unsubscribe(channel: String, listener: (ByteArray) -> Unit) {
        synchronized(listeners) {
            val channelListeners = listeners[channel] ?: return
            if (!channelListeners.remove(listener) || channelListeners.isNotEmpty()) return
            listeners.remove(channel)
            subscribed.remove(channel)
            connection.async().unsubscribe(channel).whenComplete { _, error ->
                if (error != null) logger.log(Level.WARNING, "failed to unsubscribe from redis channel $channel", error)
            }
        }
    }

    private fun deliver(channel: String, message: ByteArray) {
        listeners[channel]?.forEach { listener ->
            runCatching { listener(message) }
                .onFailure { logger.log(Level.WARNING, "redis listener on channel $channel failed", it) }
        }
    }

    private inner class Dispatcher : RedisPubSubAdapter<String, ByteArray>() {
        override fun message(channel: String, message: ByteArray) {
            try {
                executor.execute { deliver(channel, message) }
            } catch (e: RejectedExecutionException) {
                return
            }
        }
    }
}
