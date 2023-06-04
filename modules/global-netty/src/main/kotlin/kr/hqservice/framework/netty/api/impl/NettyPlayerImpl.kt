package kr.hqservice.framework.netty.api.impl

import kr.hqservice.framework.netty.api.HQNettyAPI
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

data class NettyPlayerImpl(
    private val name: String,
    private val uniqueId: UUID,
    private val channel: NettyChannel?
) : NettyPlayer, KoinComponent {
    private val api: HQNettyAPI by inject()

    override fun getName(): String {
        return name
    }

    override fun getUniqueId(): UUID {
        return uniqueId
    }

    override fun getChannel(): NettyChannel? {
        return channel
    }

    override fun sendMessage(message: String, logging: Boolean) {
        api.sendMessageToPlayer(this, message, logging)
    }
}