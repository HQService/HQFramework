package kr.hqservice.framework.velocity.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.pipeline.ConnectionState

class NettyClientDisconnectedEvent(
    wrapper: ChannelWrapper,
    state: ConnectionState
) : NettyClientConnectionEvent(wrapper, state)