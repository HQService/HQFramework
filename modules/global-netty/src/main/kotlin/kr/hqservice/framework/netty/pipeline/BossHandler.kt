package kr.hqservice.framework.netty.pipeline

import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandlerAdapter
import kr.hqservice.framework.netty.channel.ChannelWrapper

class BossHandler(
    channel: Channel
) : ChannelInboundHandlerAdapter() {

    private val channel: ChannelWrapper
    private var connectionState: ConnectionState

    init {
        this.channel = ChannelWrapper(this, channel)
        connectionState = ConnectionState.IDLE
    }

    fun setConnectionState(state: ConnectionState) {
        connectionState = state
        TODO("WRITE LOG")
    }

}