package kr.hqservice.framework.netty

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NotAPropertyPacket(value: Int) : Packet() {
    var stored = value

    override fun write(buf: ByteBuf) {
        buf.writeInt(stored)
    }

    override fun read(buf: ByteBuf) {
        stored = buf.readInt()
    }
}

class PacketRegistrationTest {
    @AfterEach
    fun unregister() {
        Direction.INBOUND.unregisterPacket(NotAPropertyPacket::class)
    }

    @Test
    fun `packet whose constructor parameter has no backing field is rejected at registration`() {
        assertThrows<IllegalArgumentException> { Direction.INBOUND.registerPacket(NotAPropertyPacket::class) }

        assertNull(Direction.INBOUND.findPacketByName(NotAPropertyPacket::class.qualifiedName!!))
    }
}
