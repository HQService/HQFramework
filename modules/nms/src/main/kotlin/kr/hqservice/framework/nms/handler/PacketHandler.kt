package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.core.extension.print
import kr.hqservice.framework.nms.wrapper.item.impl.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.packet.PacketWrapper
import kr.hqservice.framework.nms.wrapper.packet.impl.PacketPlayOutSetSlotWrapper
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class PacketHandler(
    private val player: Player
) : ChannelDuplexHandler(), KoinComponent {

    private val packetPlayOutSetSlotWrapper: PacketPlayOutSetSlotWrapper by inject { parametersOf(this) }

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