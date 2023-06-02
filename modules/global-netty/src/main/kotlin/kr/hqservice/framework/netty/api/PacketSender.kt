package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.packet.Packet

interface PacketSender {
    fun sendPacketToProxy(packet: Packet)

    fun sendPacketAll(packet: Packet)

    fun sendPacket(port: Int, packet: Packet)

    fun sendPacket(name: String, packet: Packet)
}