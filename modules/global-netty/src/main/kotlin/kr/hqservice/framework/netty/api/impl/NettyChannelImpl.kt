package kr.hqservice.framework.netty.api.impl

import kr.hqservice.framework.netty.api.HQNettyAPI
import kr.hqservice.framework.netty.api.NettyChannel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class NettyChannelImpl(
    private val port: Int,
    private val name: String
) : NettyChannel, KoinComponent {
    private val api: HQNettyAPI by inject()

    override fun getName(): String {
        return name
    }

    override fun getPort(): Int {
        return port
    }

    override fun sendMessage(message: String, logging: Boolean) {
        api.sendMessageToChannel(this, message, logging)
    }
}