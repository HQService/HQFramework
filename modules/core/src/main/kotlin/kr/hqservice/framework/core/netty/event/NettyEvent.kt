package kr.hqservice.framework.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import org.bukkit.event.Event

abstract class NettyEvent(
    async: Boolean,
    val channel: ChannelWrapper
) : Event(async)