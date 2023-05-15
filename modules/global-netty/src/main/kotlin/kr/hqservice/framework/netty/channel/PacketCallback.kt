package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.AbstractPacket

interface PacketCallback<T : AbstractPacket> {
    fun onCallbackReceived(packet: T)
}