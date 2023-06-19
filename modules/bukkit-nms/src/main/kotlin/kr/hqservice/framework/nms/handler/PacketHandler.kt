package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.entity.Player

class PacketHandler(
    private val player: Player
) : ChannelDuplexHandler() {

    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        val packetName = message.javaClass.simpleName

        if(packetName == "PacketPlayOutWindowItems") {

        }
        if(packetName == "PacketPlayOutSetSlot") {

        }

        super.write(context, message, promise)
    }

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        val packetName = message.javaClass.simpleName

        super.channelRead(context, message)
    }

}