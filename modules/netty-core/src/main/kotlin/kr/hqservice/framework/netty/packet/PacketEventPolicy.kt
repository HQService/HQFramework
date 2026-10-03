package kr.hqservice.framework.netty.packet

import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket

object PacketEventPolicy {
    fun isInternal(packet: Packet): Boolean =
        packet is PingPongPacket || packet is HandShakePacket || packet.isCallbackResult()
}
