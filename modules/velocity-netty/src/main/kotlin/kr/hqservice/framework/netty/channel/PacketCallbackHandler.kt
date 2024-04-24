package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.Packet

interface PacketCallbackHandler<T : Packet> {
    fun onCallbackReceived(packet: T)
}