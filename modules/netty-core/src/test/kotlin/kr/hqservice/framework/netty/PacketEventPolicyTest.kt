package kr.hqservice.framework.netty

import kr.hqservice.framework.netty.packet.PacketEventPolicy
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PacketEventPolicyTest {
    @Test
    fun `heartbeats, handshakes and callback results are internal and never raise plugin events`() {
        assertTrue(PacketEventPolicy.isInternal(PingPongPacket(1L, 2L)))
        assertTrue(PacketEventPolicy.isInternal(HandShakePacket(1)))
        assertTrue(PacketEventPolicy.isInternal(PingLike(1).apply { setCallbackResult(true) }))
        assertFalse(PacketEventPolicy.isInternal(PingLike(1)))
    }
}
