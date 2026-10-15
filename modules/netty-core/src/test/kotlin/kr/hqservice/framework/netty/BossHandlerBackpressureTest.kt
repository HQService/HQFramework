package kr.hqservice.framework.netty

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class BossHandlerBackpressureTest {
    @AfterEach
    fun unregister() {
        Direction.INBOUND.unregisterPacket(PingLike::class)
    }

    @Test
    fun `auto read pauses while too many packets are pending and resumes once they drain`() {
        val gate = CompletableDeferred<Unit>()
        Direction.INBOUND.addListener(PingLike::class) { _, _ -> gate.await() }
        lateinit var handler: BossHandler
        val channel = EmbeddedChannel(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                handler = BossHandler(ch, Logger.getAnonymousLogger(), null, Dispatchers.Default)
                ch.pipeline().addLast(handler)
            }
        })
        handler.connectionState = ConnectionState.CONNECTED

        repeat(1000) { channel.writeInbound(PingLike(it)) }

        assertTrue(channel.config().isAutoRead)

        repeat(100) { channel.writeInbound(PingLike(it)) }

        assertFalse(channel.config().isAutoRead)

        gate.complete(Unit)
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
        while (!channel.config().isAutoRead && System.nanoTime() < deadline) Thread.sleep(10)

        assertTrue(channel.config().isAutoRead)
    }
}
