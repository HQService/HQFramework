package kr.hqservice.framework.netty.api.impl

import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import org.koin.core.component.KoinComponent
import java.util.*

data class NettyPlayerImpl(
    private val name: String,
    private val uniqueId: UUID,
    private val channel: NettyChannel?
) : NettyPlayer, KoinComponent {
    override fun getName(): String {
        return name
    }

    override fun getUniqueId(): UUID {
        return uniqueId
    }

    override fun getChannel(): NettyChannel? {
        return channel
    }
}