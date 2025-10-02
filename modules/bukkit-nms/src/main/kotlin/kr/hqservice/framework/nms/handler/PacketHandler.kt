package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.plugin.Plugin
import java.util.UUID

class PacketHandler(
    private val uniqueId: UUID,
    private val plugin: Plugin,
    private val virtualHandlerRegistry: VirtualHandlerRegistry
) : ChannelDuplexHandler() {
    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        val result = write0(context, message, promise)
        if (result == null) return

        if (result.isNotEmpty()) {
            result.forEach { super.write(context, it, promise) }
        } else super.write(context, message, promise)
    }

    fun write0(context: ChannelHandlerContext, message: Any, promise: ChannelPromise): List<Any>? {
        /*if (!first) {
            val player = plugin.server.getPlayer(uniqueId)
            if (player != null) {
                plugin.server.pluginManager.callEvent(PlayerDataPreLoadEvent(player))
            }
        }*/

        virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter {
                (it.unregisterType() == HandlerUnregisterType.WRITE
                        || it.unregisterType() == HandlerUnregisterType.ALL)
                        && it.unregisterCondition(message)
            }.forEach { virtualHandlerRegistry.unregister(uniqueId, it) }

        return virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter { it.checkCondition(message) }
            .let {
                it.forEach { handler -> handler.handle(message) }
                it.mapNotNull { handler -> handler.cancelParent(message) }
            }
    }

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        val result = channelRead0(context, message)
        if (result.isNotEmpty()) {
            result.forEach { super.channelRead(context, it) }
        } else super.channelRead(context, message)
    }

    fun channelRead0(context: ChannelHandlerContext, message: Any): List<Any> {
        virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter {
                (it.unregisterType() == HandlerUnregisterType.READ
                        || it.unregisterType() == HandlerUnregisterType.ALL)
                        && it.unregisterCondition(message)
            }.forEach { virtualHandlerRegistry.unregister(uniqueId, it) }

        return virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter { it.checkCondition(message) }
            .let {
                it.forEach { handler -> handler.handle(message) }
                it.mapNotNull { handler -> handler.cancelParent(message) }
            }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (ctx.pipeline().get("hq_injector") != null) {
            virtualHandlerRegistry.cleanup(uniqueId)
            ctx.pipeline().remove("hq_injector")
        }
    }
}