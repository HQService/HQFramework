package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.channel.PacketCallbackHandler
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import java.time.Duration
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
                handler = BossHandler(ch, Logger.getAnonymousLogger())
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
}
