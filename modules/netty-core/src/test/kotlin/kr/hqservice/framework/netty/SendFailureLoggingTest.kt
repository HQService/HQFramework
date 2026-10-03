package kr.hqservice.framework.netty

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

class SendFailureLoggingTest {
    private val records = mutableListOf<LogRecord>()
    private val logger = Logger.getLogger("SendFailureLoggingTest").apply {
        useParentHandlers = false
        addHandler(object : Handler() {
            override fun publish(record: LogRecord) { records += record }
            override fun flush() {}
            override fun close() {}
        })
    }

    @BeforeEach
    fun register() {
        Direction.OUTBOUND.registerPacket(PingLike::class)
    }

    @AfterEach
    fun unregister() {
        Direction.OUTBOUND.unregisterPacket(PingLike::class)
    }

    @Test
    fun `a write that fails after being queued is logged with its cause`() {
        lateinit var handler: BossHandler
        EmbeddedChannel(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                handler = BossHandler(ch, logger, null, Dispatchers.Default)
                ch.pipeline().addLast(object : ChannelOutboundHandlerAdapter() {
                    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                        promise.setFailure(IllegalStateException("socket gone"))
                    }
                })
                ch.pipeline().addLast(handler)
            }
        })
        handler.connectionState = ConnectionState.CONNECTED

        handler.channel.sendPacket(PingLike(3))

        val warning = records.firstOrNull { it.level == Level.WARNING && it.message.contains("PingLike") }
        assertTrue(warning != null && warning.thrown?.message == "socket gone", records.map { it.message }.toString())
    }
}
