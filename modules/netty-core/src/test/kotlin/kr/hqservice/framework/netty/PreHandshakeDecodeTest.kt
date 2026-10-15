package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.writeString
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.logging.Logger

object ReadProbeTracker {
    @Volatile
    var read = false
}

class ReadProbe(var n: Int) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeInt(n)
    }

    override fun read(buf: ByteBuf) {
        ReadProbeTracker.read = true
        n = buf.readInt()
    }
}

class PreHandshakeDecodeTest {
    @BeforeEach
    fun register() {
        ReadProbeTracker.read = false
        Direction.INBOUND.registerPacket(ReadProbe::class)
        Direction.INBOUND.registerPacket(HandShakePacket::class)
    }

    @AfterEach
    fun unregister() {
        Direction.INBOUND.unregisterPacket(ReadProbe::class)
        Direction.INBOUND.unregisterPacket(HandShakePacket::class)
    }

    private fun serverChannel() = EmbeddedChannel(object : ChannelInitializer<Channel>() {
        override fun initChannel(ch: Channel) {
            HQChannelInitializer(Logger.getAnonymousLogger(), Dispatchers.Default, true, "s3cret").installPipeline(ch)
        }
    })

    private fun frame(packetName: String, body: ByteBuf.() -> Unit): ByteBuf {
        val payload = Unpooled.buffer().apply { writeString(packetName) }.writeBoolean(false).apply(body)
        return Unpooled.buffer().writeLong(payload.readableBytes().toLong()).writeBytes(payload)
    }

    private fun probeFrame() = frame(ReadProbe::class.qualifiedName!!) { writeInt(7) }

    @Test
    fun `non-handshake frame is not decoded before the handshake`() {
        val channel = serverChannel()

        channel.writeInbound(probeFrame())

        assertNull(channel.readInbound<Any>())
        assertFalse(ReadProbeTracker.read)
    }

    @Test
    fun `handshake is decoded before connecting and opens the gate`() {
        val channel = serverChannel()

        channel.writeInbound(frame(HandShakePacket::class.qualifiedName!!) {
            writeInt(25565)
            writeString("s3cret")
        })

        assertEquals(ConnectionState.CONNECTED, channel.pipeline().get(BossHandler::class.java).connectionState)

        channel.writeInbound(probeFrame())

        assertTrue(ReadProbeTracker.read)
    }
}
