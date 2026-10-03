package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.channel.PacketCallbackHandler
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CyclicBarrier
import java.util.logging.Logger

class PingLike(var n: Int) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeInt(n)
    }

    override fun read(buf: ByteBuf) {
        n = buf.readInt()
    }
}

class CallbackContainerTest {
    @BeforeEach
    fun register() {
        Direction.OUTBOUND.registerPacket(PingLike::class)
    }

    @AfterEach
    fun unregister() {
        Direction.OUTBOUND.unregisterPacket(PingLike::class)
    }

    private fun wrapperIn(state: ConnectionState): ChannelWrapper {
        lateinit var handler: BossHandler
        EmbeddedChannel(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                handler = BossHandler(ch, Logger.getAnonymousLogger(), null, Dispatchers.Default)
                ch.pipeline().addLast(handler)
            }
        })
        handler.connectionState = state
        return handler.channel
    }

    private fun callback(onReceived: (PingLike) -> Unit) = object : PacketCallbackHandler<PingLike> {
        override fun onCallbackReceived(packet: PingLike) = onReceived(packet)
    }

    @Test
    fun `callback runs outside the lock so it can start another callback`() {
        val wrapper = wrapperIn(ConnectionState.CONNECTED)
        val container = wrapper.callbackContainer

        container.addOnQueue(wrapper, PingLike(1), PingLike::class, callback {
            container.addOnQueue(wrapper, PingLike(2), PingLike::class, callback { })
        })

        val completed = assertTimeoutPreemptively(Duration.ofSeconds(2), ThrowingSupplier {
            container.complete(PingLike(1).apply { setCallbackResult(true) })
        })
        assertTrue(completed)
    }

    @Test
    fun `callback is not queued when the packet was dropped`() {
        val wrapper = wrapperIn(ConnectionState.IDLE)
        val container = wrapper.callbackContainer

        container.addOnQueue(wrapper, PingLike(1), PingLike::class, callback { })

        assertFalse(container.complete(PingLike(1).apply { setCallbackResult(true) }))
    }

    @Test
    fun `concurrent callbacks are completed in the order their packets were written`() {
        val wrapper = wrapperIn(ConnectionState.CONNECTED)
        val container = wrapper.callbackContainer
        val channel = wrapper.channel as EmbeddedChannel
        val received = ConcurrentHashMap<Int, Int>()
        val barrier = CyclicBarrier(2)

        val threads = (0 until 2).map { thread ->
            Thread {
                barrier.await()
                repeat(100) { i ->
                    val marker = thread * 1000 + i
                    container.addOnQueue(wrapper, PingLike(marker), PingLike::class, callback { received[marker] = it.n })
                }
            }.apply { start() }
        }
        threads.forEach { it.join() }

        val written = generateSequence { channel.readOutbound<PingLike>() }.map { it.n }.toList()
        assertEquals(200, written.size)
        written.forEach { container.complete(PingLike(it).apply { setCallbackResult(true) }) }

        assertEquals(200, received.size)
        received.forEach { (marker, reply) -> assertEquals(marker, reply) }
    }

    @Test
    fun `a callback that is never answered is dropped after its timeout`() {
        val wrapper = wrapperIn(ConnectionState.CONNECTED)
        var answered = false
        wrapper.callbackContainer.addOnQueue(wrapper, PingLike(1), PingLike::class, callback { answered = true }, timeoutMs = 50)
        val embedded = wrapper.channel as EmbeddedChannel
        embedded.readOutbound<Any>()

        Thread.sleep(120)
        embedded.runScheduledPendingTasks()

        assertFalse(wrapper.callbackContainer.complete(PingLike(1).apply { setCallbackResult(true) }))
        assertFalse(answered)
    }

    @Test
    fun `pending callbacks are discarded when the channel closes`() {
        val wrapper = wrapperIn(ConnectionState.CONNECTED)
        var answered = false
        wrapper.callbackContainer.addOnQueue(wrapper, PingLike(1), PingLike::class, callback { answered = true })
        val embedded = wrapper.channel as EmbeddedChannel
        embedded.readOutbound<Any>()

        embedded.close().syncUninterruptibly()

        assertFalse(wrapper.callbackContainer.complete(PingLike(1).apply { setCallbackResult(true) }))
        assertFalse(answered)
    }
}
