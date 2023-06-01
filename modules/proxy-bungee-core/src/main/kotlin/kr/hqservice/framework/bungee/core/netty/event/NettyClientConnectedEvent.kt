package kr.hqservice.framework.bungee.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.pipeline.ConnectionState

class NettyClientConnectedEvent(
    wrapper: ChannelWrapper,
    state: ConnectionState,
    val serverName: String
) : NettyClientConnectionEvent(wrapper, state)