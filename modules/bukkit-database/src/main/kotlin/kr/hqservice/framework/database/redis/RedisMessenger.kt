package kr.hqservice.framework.database.redis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean
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

    fun <T> subscribe(channel: String, serializer: KSerializer<T>, scope: CoroutineScope, handler: suspend (T) -> Unit): Subscription {
        val prefixed = settings.key("msg", channel)
        val registration = transport.subscribe(prefixed) { payload ->
            val value = runCatching { json.decodeFromString(serializer, payload.decodeToString()) }
                .onFailure { logger.log(Level.WARNING, "dropped malformed redis message on $prefixed", it) }
                .getOrNull() ?: return@subscribe
            scope.launch { handler(value) }
        }
        val subscription = ScopedSubscription(registration)
        scope.coroutineContext[Job]?.let { subscription.bindTo(it) }
        return subscription
    }
}

private class ScopedSubscription(private val registration: AutoCloseable) : Subscription {
    private val closed = AtomicBoolean()

    @Volatile
    private var completion: DisposableHandle? = null

    fun bindTo(job: Job) {
        val handle = job.invokeOnCompletion { close() }
        completion = handle
        if (closed.get()) handle.dispose()
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        completion?.dispose()
        registration.close()
    }
}

interface PubSubTransport : AutoCloseable {
    fun publish(channel: String, payload: ByteArray)

    fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable

    override fun close() {}
}

fun interface Subscription : AutoCloseable
