package kr.hqservice.framework.netty

import io.netty.buffer.Unpooled
import kr.hqservice.framework.netty.packet.extension.writeString
import kr.hqservice.framework.netty.packet.server.RelayPolicy
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RelayPolicyTest {
    private fun relayOf(packetName: String): ByteArray {
        val buf = Unpooled.buffer()
        buf.writeString(packetName)
        buf.writeBoolean(false)
        buf.writeBytes(byteArrayOf(1, 2, 3))
        val bytes = ByteArray(buf.readableBytes())
        buf.readBytes(bytes)
        buf.release()
        return bytes
    }

    @Test
    fun `server-only packet is not relayable`() {
        assertFalse(RelayPolicy.isRelayable(relayOf(ShutdownPacket::class.qualifiedName!!)))
    }

    @Test
    fun `user packet is relayable`() {
        assertTrue(RelayPolicy.isRelayable(relayOf("kr.example.MyPacket")))
    }

    @Test
    fun `empty relay is not relayable`() {
        assertFalse(RelayPolicy.isRelayable(ByteArray(0)))
    }
}
