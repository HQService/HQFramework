package kr.hqservice.framework.netty

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.CoroutineDispatcher
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

class PingInlineTest {
    private val stuckLane = object : CoroutineDispatcher() {
        var queued = 0
        override fun dispatch(context: CoroutineContext, block: Runnable) { queued++ }
    }
    private lateinit var handler: BossHandler
    private lateinit var channel: EmbeddedChannel

    @BeforeEach
    fun setUp() {
        Direction.INBOUND.registerPacket(PingPongPacket::class)
        Direction.OUTBOUND.registerPacket(PingPongPacket::class)
        Direction.INBOUND.registerPacket(PingLike::class)
        channel = EmbeddedChannel(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                handler = BossHandler(ch, Logger.getAnonymousLogger(), null, stuckLane)
                ch.pipeline().addLast(handler)
            }
        })
        handler.connectionState = ConnectionState.CONNECTED
    }

    @AfterEach
    fun tearDown() {
        Direction.INBOUND.unregisterPacket(PingPongPacket::class)
        Direction.OUTBOUND.unregisterPacket(PingPongPacket::class)
        Direction.INBOUND.unregisterPacket(PingLike::class)
    }

    @Test
    fun `ping requests are answered on the event loop even when the handler lane is stuck`() {
        Direction.INBOUND.addListener(PingPongPacket::class) { packet, wrapper -> wrapper.channel.writeAndFlush(PingPongPacket(packet.time, 7L)) }

        channel.writeInbound(PingLike(1))
        channel.writeInbound(PingPongPacket(-1L, 42L))

        val reply = channel.readOutbound<PingPongPacket>()
        assertNotNull(reply, "the ping reply must not wait behind the stuck lane")
        assertEquals(42L, reply.receivedTime)
        assertEquals(1, stuckLane.queued, "ordinary packets still go to the lane")
    }

    @Test
    fun `ping callback results complete their callback inline`() {
        var received: PingPongPacket? = null
        handler.channel.startCallback(PingPongPacket(-1L, 1L), PingPongPacket::class) { received = it }
        channel.readOutbound<Any>()

        channel.writeInbound(PingPongPacket(5L, 1L).apply { setCallbackResult(true) })

        assertNotNull(received)
        assertTrue(stuckLane.queued == 0)
    }
}
