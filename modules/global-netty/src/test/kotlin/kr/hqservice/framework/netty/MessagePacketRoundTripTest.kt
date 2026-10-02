package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import kr.hqservice.framework.netty.pipeline.MAX_FRAME_BYTES
import kr.hqservice.framework.netty.pipeline.PacketDecoder
import kr.hqservice.framework.netty.pipeline.PacketEncoder
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessagePacketRoundTripTest {
    @BeforeEach
    fun register() {
        Direction.INBOUND.registerPacket(MessagePacket::class)
        Direction.OUTBOUND.registerPacket(MessagePacket::class)
        Direction.INBOUND.registerPacket(BroadcastPacket::class)
        Direction.OUTBOUND.registerPacket(BroadcastPacket::class)
    }

    @AfterEach
    fun unregister() {
        Direction.values().forEach {
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
}
