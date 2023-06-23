package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.Named
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

class PacketHandler(
    player: Player,
    private val virtualHandlerRegistry: VirtualHandlerRegistry
) : ChannelDuplexHandler() {
    private val uniqueId = player.uniqueId
    private val name = player.name

    @Suppress("unchecked_cast")
    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        val handlers = virtualHandlerRegistry.getHandlers(uniqueId)
        handlers.filter { it.checkCondition(message) }
            .forEach { it.handle(message) }

        super.write(context, message, promise)
    }

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        val packetName = message.javaClass.simpleName
        val handlers = virtualHandlerRegistry.getHandlers(uniqueId)
        handlers.filter { it.unregisterCondition(message) }
            .forEach { virtualHandlerRegistry.unregister(uniqueId, it) }
        super.channelRead(context, message)
    }

}