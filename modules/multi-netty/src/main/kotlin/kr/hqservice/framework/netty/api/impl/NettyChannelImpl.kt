package kr.hqservice.framework.netty.api.impl

import kr.hqservice.framework.netty.api.NettyChannel

data class NettyChannelImpl(
    private val port: Int,
    private val name: String
) : NettyChannel {
    override fun getName(): String {
        return name
    }

    override fun getPort(): Int {
        return port
    }
}