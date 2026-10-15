package kr.hqservice.framework.database.redis

import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.SocketOptions
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class RedisProvider(private val settings: RedisSettings, private val logger: Logger) : AutoCloseable {
    private val codec: RedisCodec<String, ByteArray> = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
    private var client: RedisClient? = if (settings.enabled) createClient(settings.uri) else null
    private var connection: StatefulRedisConnection<String, ByteArray>? = null
    private var pubSubConnection: StatefulRedisPubSubConnection<String, ByteArray>? = null
    private var closed = false

    val enabled: Boolean get() = settings.enabled

    @Synchronized
    fun connection(): StatefulRedisConnection<String, ByteArray> =
        connection ?: client().connect(codec).also { connection = it }

    @Synchronized
    fun pubSubConnection(): StatefulRedisPubSubConnection<String, ByteArray> =
        pubSubConnection ?: client().connectPubSub(codec).also { pubSubConnection = it }

    private fun client(): RedisClient {
        check(enabled) { "redis.uri is not configured" }
        check(!closed) { "redis provider is closed" }
        return client!!
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        listOfNotNull(pubSubConnection, connection).forEach { opened ->
            runCatching { opened.close() }.onFailure { logger.log(Level.WARNING, "failed to close redis connection", it) }
        }
        client?.let { opened ->
            runCatching { opened.shutdown(0, 2, TimeUnit.SECONDS) }.onFailure { logger.log(Level.WARNING, "failed to shut down redis client", it) }
        }
        pubSubConnection = null
        connection = null
        client = null
    }

    private companion object {
        val TIMEOUT: Duration = Duration.ofSeconds(3)

        fun createClient(uri: String): RedisClient {
            val redisUri = try {
                RedisURI.create(uri)
            } catch (e: RuntimeException) {
                throw IllegalStateException("redis.uri is not a valid redis uri", e)
            }
            redisUri.timeout = TIMEOUT
            return RedisClient.create(redisUri).apply {
                options = ClientOptions.builder()
                    .timeoutOptions(TimeoutOptions.enabled(TIMEOUT))
                    .socketOptions(SocketOptions.builder().connectTimeout(TIMEOUT).build())
                    .build()
            }
        }
    }
}
