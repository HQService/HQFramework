package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.AbstractPacket

interface PacketCallbackHandler<T : AbstractPacket> {
    fun onCallbackReceived(packet: T)
}