package kr.hqservice.framework.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.AbstractPacket
import org.bukkit.event.Event

abstract class PacketEvent(
    async: Boolean,
    channel: ChannelWrapper,
    val packet: AbstractPacket
) : NettyEvent(async, channel)