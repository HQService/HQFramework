package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.nms.event.PlayerDataPreLoadEvent
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.logging.Level

class PacketHandler(
    private val uniqueId: UUID,
    private val plugin: Plugin,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,
    private val disconnectPacketSupplier: (String) -> Any
) : ChannelDuplexHandler() {
    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        if (message::class.simpleName == "PacketLoginOutSuccess") {
            // pre login
            val event = PlayerDataPreLoadEvent(uniqueId)
            try {
                plugin.server.pluginManager.callEvent(event)
                if (event.isInvalid()) {
                    virtualHandlerRegistry.cleanup(uniqueId)
                    super.write(context, disconnectPacketSupplier.invoke(event.getFailReason()), promise)
                    return
                }
            } catch (e: Exception) {
                virtualHandlerRegistry.cleanup(uniqueId)
                super.write(context, disconnectPacketSupplier.invoke("데이터 로드 실패"), promise)
                Bukkit.getLogger().log(Level.SEVERE, "~~", e)
                return
            }

            super.write(context, message, promise)
            return
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