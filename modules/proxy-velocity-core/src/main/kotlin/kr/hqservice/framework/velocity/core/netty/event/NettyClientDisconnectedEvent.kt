package kr.hqservice.framework.velocity.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.framework.velocity.core.netty.event.NettyClientConnectionEvent

class NettyClientDisconnectedEvent(
    wrapper: ChannelWrapper,
    state: ConnectionState
) : NettyClientConnectionEvent(wrapper, state)