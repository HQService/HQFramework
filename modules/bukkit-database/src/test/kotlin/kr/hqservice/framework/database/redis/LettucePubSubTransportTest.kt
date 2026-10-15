package kr.hqservice.framework.database.redis

import io.lettuce.core.RedisFuture
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.logging.Level
import java.util.logging.Logger

class LettucePubSubTransportTest {
    private val logger = mockk<Logger>(relaxed = true)
    private val adapter = slot<RedisPubSubListener<String, ByteArray>>()
    private val commands = mockk<RedisPubSubAsyncCommands<String, ByteArray>>()
    private val connection = mockk<StatefulRedisPubSubConnection<String, ByteArray>>().also {
        every { it.addListener(capture(adapter)) } just Runs
        every { it.async() } returns commands
    }
    private val provider = mockk<RedisProvider>().also { every { it.pubSubConnection() } returns connection }
    private val transport = LettucePubSubTransport(provider, logger)

    @AfterEach
    fun tearDown() {
        transport.close()
    }

    private fun completed(error: Throwable? = null): RedisFuture<Void> = mockk<RedisFuture<Void>>().also { future ->
        every { future.whenComplete(any()) } answers {
            firstArg<BiConsumer<Void?, Throwable?>>().accept(null, error)
            future
        }
    }

    @Test
    fun `a throwing listener does not stop the next listener and listeners run off the redis thread`() {
        every { commands.subscribe(*anyVararg()) } returns completed()
        val received = CompletableFuture<Pair<String, String>>()
        transport.subscribe("ch") { throw IllegalStateException("broken listener") }
        transport.subscribe("ch") { received.complete(it.decodeToString() to Thread.currentThread().name) }

        adapter.captured.message("ch", "hello".toByteArray())

        val (payload, thread) = received.get(2, TimeUnit.SECONDS)
        assertEquals("hello", payload)
        assertNotEquals(Thread.currentThread().name, thread)
        verify(timeout = 2000) { logger.log(Level.WARNING, match<String> { it.contains("ch") }, any<IllegalStateException>()) }
    }

    @Test
    fun `failed subscribe is retried by the next subscription and keeps earlier listeners`() {
        every { commands.subscribe(*anyVararg()) } returnsMany listOf(completed(IllegalStateException("down")), completed())
        val first = CompletableFuture<String>()
        val second = CompletableFuture<String>()

        transport.subscribe("ch") { first.complete(it.decodeToString()) }
        transport.subscribe("ch") { second.complete(it.decodeToString()) }
        adapter.captured.message("ch", "hi".toByteArray())

        verify(exactly = 2) { commands.subscribe("ch") }
        assertEquals("hi", first.get(2, TimeUnit.SECONDS))
        assertEquals("hi", second.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `unsubscribe failure is logged`() {
        every { commands.subscribe(*anyVararg()) } returns completed()
        every { commands.unsubscribe(*anyVararg()) } returns completed(IllegalStateException("down"))

        transport.subscribe("ch") {}.close()

        verify { logger.log(Level.WARNING, match<String> { it.contains("unsubscribe") }, any<IllegalStateException>()) }
    }
}
