package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PacketHandler(
    private val player: Player
) : ChannelDuplexHandler(), KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

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