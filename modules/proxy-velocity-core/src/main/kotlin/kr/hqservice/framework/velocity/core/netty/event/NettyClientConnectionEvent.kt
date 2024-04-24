package kr.hqservice.framework.velocity.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.pipeline.ConnectionState

open class NettyClientConnectionEvent(
    val wrapper: ChannelWrapper,
    val state: ConnectionState
) : NettyEvent()