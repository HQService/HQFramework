package kr.hqservice.framework.bukkit.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import org.bukkit.event.HandlerList

class NettyClientConnectedEvent(
    channel: ChannelWrapper
) : NettyEvent(false, channel) {
    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

    companion object {
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }
}