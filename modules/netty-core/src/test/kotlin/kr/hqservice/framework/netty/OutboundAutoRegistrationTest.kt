package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.embedded.EmbeddedChannel
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.pipeline.PacketEncoder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnannouncedPacket(val n: Int) : Packet() {
    override fun write(buf: ByteBuf) { buf.writeInt(n) }
    override fun read(buf: ByteBuf) {}
}

class OutboundAutoRegistrationTest {
    @AfterEach
    fun cleanUp() {
        Direction.OUTBOUND.unregisterPacket(UnannouncedPacket::class)
    }

    @Test
    fun `sending a packet that was never registered outbound registers it on first use`() {
        assertNull(Direction.OUTBOUND.findPacketByClass(UnannouncedPacket::class))
        val channel = EmbeddedChannel(PacketEncoder())

        assertTrue(channel.writeOutbound(UnannouncedPacket(3)))

        assertNotNull(Direction.OUTBOUND.findPacketByClass(UnannouncedPacket::class))
        val frame = channel.readOutbound<ByteBuf>()
        assertTrue(frame.readableBytes() > 4)
        frame.release()
    }
}
