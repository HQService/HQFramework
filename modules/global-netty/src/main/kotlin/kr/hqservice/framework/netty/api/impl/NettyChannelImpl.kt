package kr.hqservice.framework.netty.api.impl

import kr.hqservice.framework.netty.api.NettyChannel
import org.koin.core.component.KoinComponent

data class NettyChannelImpl(
    private val port: Int,
    private val name: String
) : NettyChannel, KoinComponent {
    override fun getName(): String {
        return name
    }

    override fun getPort(): Int {
        return port
    }
}