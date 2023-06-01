package kr.hqservice.framework.bukkit.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.AbstractPacket

abstract class PacketEvent(
    async: Boolean,
    channel: ChannelWrapper,
    val packet: AbstractPacket
) : NettyEvent(async, channel)