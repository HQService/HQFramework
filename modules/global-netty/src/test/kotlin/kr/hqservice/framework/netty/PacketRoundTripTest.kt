package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.extension.writeString
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.pipeline.MAX_FRAME_BYTES
import kr.hqservice.framework.netty.pipeline.PacketDecoder
import kr.hqservice.framework.netty.pipeline.PacketEncoder
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

object StaticInitTracker {
    @Volatile
    var initialized = false
}

class StaticInitProbe {
    companion object {
        init {
            StaticInitTracker.initialized = true
        }
    }
}

class PacketRoundTripTest {
    @BeforeEach
    fun register() {
        Direction.INBOUND.registerPacket(HandShakePacket::class)
        Direction.OUTBOUND.registerPacket(HandShakePacket::class)
        Direction.INBOUND.registerPacket(MessagePacket::class)
        Direction.OUTBOUND.registerPacket(MessagePacket::class)
        Direction.INBOUND.registerPacket(BroadcastPacket::class)
        Direction.OUTBOUND.registerPacket(BroadcastPacket::class)
    }

    @AfterEach
    fun unregister() {
        Direction.values().forEach {
            it.unregisterPacket(HandShakePacket::class)
            it.unregisterPacket(MessagePacket::class)
            it.unregisterPacket(BroadcastPacket::class)
        }
    }

    private fun redHelloWorld() = TextComponent("Hello world").apply { color = ChatColor.RED }

    private fun pipeline() = EmbeddedChannel(
        LengthFieldPrepender(8),
        LengthFieldBasedFrameDecoder(MAX_FRAME_BYTES, 0, 8, 0, 8),
        PacketDecoder(),
        PacketEncoder()
    )

    private fun frameOf(packetName: String): ByteBuf {
        val payload = Unpooled.buffer()
            .apply { writeString(packetName) }
            .writeBoolean(false)
            .writeBytes(byteArrayOf(1, 2, 3, 4))
        return Unpooled.buffer()
            .writeLong(payload.readableBytes().toLong())
            .writeBytes(payload)
    }

    private fun <T> EmbeddedChannel.roundTrip(packet: Any): T {
        writeOutbound(packet)
        val frame = Unpooled.buffer()
        while (true) {
            val part = readOutbound<ByteBuf>() ?: break
            frame.writeBytes(part)
            part.release()
        }
        writeInbound(frame)
        return readInbound<T>()
    }

    @Test
    fun `handshake survives a round trip`() {
        val decoded = pipeline().roundTrip<HandShakePacket>(HandShakePacket(25565))
        assertEquals(25565, decoded.port)
    }

    @Test
    fun `message packet carries a styled component with spaces`() {
        val component = redHelloWorld()
        val decoded = pipeline().roundTrip<MessagePacket>(MessagePacket(component, false, emptyList()))
        assertEquals(ComponentSerializer.toString(component), ComponentSerializer.toString(decoded.message))
    }

    @Test
    fun `broadcast packet carries a styled component with spaces`() {
        val component = redHelloWorld()
        val decoded = pipeline().roundTrip<BroadcastPacket>(BroadcastPacket(component, false, null))
        assertEquals(ComponentSerializer.toString(component), ComponentSerializer.toString(decoded.message))
    }

    @Test
    fun `unknown packet name is dropped and the channel stays open`() {
        val channel = pipeline()

        channel.writeInbound(frameOf("kr.hqservice.nope.Missing"))

        assertNull(channel.readInbound<Any>())
        assertTrue(channel.isOpen)
        assertNull(Direction.INBOUND.findPacketByName("kr.hqservice.nope.Missing"))
    }

    @Test
    fun `unregistered class name is never loaded`() {
        val channel = pipeline()

        channel.writeInbound(frameOf(StaticInitProbe::class.java.name))

        assertNull(channel.readInbound<Any>())
        assertTrue(channel.isOpen)
        assertFalse(StaticInitTracker.initialized)
    }
}
