package kr.hqservice.framework.netty

import io.mockk.mockk
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IsolationPacket(val n: Int) : Packet() {
    override fun write(buf: ByteBuf) { buf.writeInt(n) }
    override fun read(buf: ByteBuf) {}
}

class PacketHandlerIsolationTest {
    @AfterEach
    fun unregister() {
        Direction.INBOUND.unregisterPacket(IsolationPacket::class)
    }

    @Test
    fun `a throwing handler does not stop the handlers registered after it`() = runBlocking {
        Direction.INBOUND.registerPacket(IsolationPacket::class)
        val seen = mutableListOf<String>()
        Direction.INBOUND.addListener(IsolationPacket::class) { _, _ -> seen += "first"; error("first handler broke") }
        Direction.INBOUND.addListener(IsolationPacket::class) { packet, _ -> seen += "second:${packet.n}" }

        val handled = Direction.INBOUND.onPacketReceived(IsolationPacket(7), mockk<ChannelWrapper>(relaxed = true))

        assertTrue(handled)
        assertEquals(listOf("first", "second:7"), seen)
    }
}
