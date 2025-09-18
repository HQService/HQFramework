package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.plugin.Plugin
import java.util.UUID

class EarlyPacketHandler(
    private val plugin: Plugin,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,

    private val uniqueIdProvider: (Any) -> UUID?,
) : ChannelDuplexHandler() {
    private var child: PacketHandler? = null

    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        val result = child?.write0(context, message, promise) ?: emptyList()
        if (result.isNotEmpty()) {
            result.forEach { super.write(context, it, promise) }
        } else super.write(context, message, promise)
    }

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var child = child
        val result = if (child == null) {
            val uniqueId = uniqueIdProvider(message)
            if (uniqueId != null) {
                child = PacketHandler(uniqueId, plugin, virtualHandlerRegistry)
                this.child = child

                child.channelRead0(context, message)
            } else emptyList()
        } else {
            child.channelRead0(context, message)
        }

        if (result.isNotEmpty()) {
            result.forEach { super.channelRead(context, it) }
        } else super.channelRead(context, message)
    }
}