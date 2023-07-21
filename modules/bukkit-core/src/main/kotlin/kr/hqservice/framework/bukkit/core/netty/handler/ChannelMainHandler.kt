package kr.hqservice.framework.bukkit.core.netty.handler

import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.PacketHandler
import kr.hqservice.framework.netty.packet.server.HandShakePacket

interface ChannelMainHandler : PacketHandler<HandShakePacket>, PacketSender, HQSimpleComponent {
    fun disconnect()
}