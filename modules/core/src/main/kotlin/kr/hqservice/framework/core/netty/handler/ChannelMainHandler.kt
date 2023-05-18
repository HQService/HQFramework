package kr.hqservice.framework.core.netty.handler

import kr.hqservice.framework.core.component.HQSimpleComponent
import kr.hqservice.framework.netty.packet.PacketHandler
import kr.hqservice.framework.netty.packet.server.HandShakePacket

interface ChannelMainHandler : PacketHandler<HandShakePacket>, PacketSender, HQSimpleComponent {
    fun disconnect()
}