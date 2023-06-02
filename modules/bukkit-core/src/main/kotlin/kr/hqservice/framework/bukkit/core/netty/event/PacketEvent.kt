package kr.hqservice.framework.bukkit.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet

abstract class PacketEvent(
    async: Boolean,
    channel: ChannelWrapper,
    val packet: Packet
) : NettyEvent(async, channel)