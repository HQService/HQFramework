package kr.hqservice.framework.database.redis

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.util.logging.Level
import java.util.logging.Logger

class RedisMessenger(
    private val transport: PubSubTransport,
    private val settings: RedisSettings,
    private val json: Json,
    private val logger: Logger,
) {
    fun <T> publish(channel: String, serializer: KSerializer<T>, value: T) {
        transport.publish(settings.key("msg", channel), json.encodeToString(serializer, value).toByteArray())
    }

    fun <T> subscribe(channel: String, serializer: KSerializer<T>, handler: (T) -> Unit): Subscription {
        val prefixed = settings.key("msg", channel)
        val registration = transport.subscribe(prefixed) { payload ->
            val value = runCatching { json.decodeFromString(serializer, payload.decodeToString()) }
                .onFailure { logger.log(Level.WARNING, "dropped malformed redis message on $prefixed", it) }
                .getOrNull() ?: return@subscribe
            handler(value)
        }
        return Subscription { registration.close() }
    }
}

interface PubSubTransport : AutoCloseable {
    fun publish(channel: String, payload: ByteArray)

    fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable

    override fun close() {}
}

fun interface Subscription : AutoCloseable
