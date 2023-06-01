package kr.hqservice.framework.bukkit.core.netty.event

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.AbstractPacket
import org.bukkit.event.HandlerList

class NettyPacketReceivedEvent(
    channel: ChannelWrapper,
    packet: AbstractPacket
) : PacketEvent(false, channel, packet) {
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