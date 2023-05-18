package kr.hqservice.framework.core.netty.handler

import kr.hqservice.framework.netty.packet.AbstractPacket

interface PacketSender {
    fun sendPacketToProxy(packet: AbstractPacket)

    fun sendPacketAll(packet: AbstractPacket)

    fun sendPacket(port: Int, packet: AbstractPacket)

    fun sendPacket(name: String, packet: AbstractPacket)
}