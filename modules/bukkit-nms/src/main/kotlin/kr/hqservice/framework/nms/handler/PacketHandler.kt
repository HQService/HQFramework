package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.nms.event.PlayerDataPreLoadEvent
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PacketHandler(
    private val player: Player,
    private val plugin: Plugin,
    private val virtualHandlerRegistry: VirtualHandlerRegistry
) : ChannelDuplexHandler() {
    private val uniqueId = player.uniqueId
    private var first = false

    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        if (!first) {
            first = true
            plugin.server.pluginManager.callEvent(PlayerDataPreLoadEvent(player))
        }

        virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter {
                (it.unregisterType() == HandlerUnregisterType.WRITE
                        || it.unregisterType() == HandlerUnregisterType.ALL)
                        && it.unregisterCondition(message)
            }.forEach { virtualHandlerRegistry.unregister(uniqueId, it) }

        virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter { it.checkCondition(message) }
            .forEach { it.handle(message) }

        super.write(context, message, promise)
    }

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter {
                (it.unregisterType() == HandlerUnregisterType.READ
                        || it.unregisterType() == HandlerUnregisterType.ALL)
                        && it.unregisterCondition(message)
            }.forEach { virtualHandlerRegistry.unregister(uniqueId, it) }

        virtualHandlerRegistry
            .getHandlers(uniqueId)
            .filter { it.checkCondition(message) }
            .forEach { it.handle(message) }
        super.channelRead(context, message)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (ctx.pipeline().get("hq_injector") != null) {
            virtualHandlerRegistry.cleanup(uniqueId)
            ctx.pipeline().remove("hq_injector")
        }
    }
}