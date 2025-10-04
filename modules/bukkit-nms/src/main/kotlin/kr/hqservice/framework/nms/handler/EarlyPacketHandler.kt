package kr.hqservice.framework.nms.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.virtual.registry.impl.VirtualHandlerRegistryImpl
import org.bukkit.plugin.Plugin
import java.util.UUID

class EarlyPacketHandler(
    private val plugin: Plugin,
    private val virtualHandlerRegistry: VirtualHandlerRegistry,

    legacy: Boolean = false,
    private val uniqueIdProvider: (Any) -> UUID?,
) : ChannelDuplexHandler() {
    private var uniqueId: UUID? = null
    private var first = false
    private var child: PacketHandler? = null

    init {
        if (legacy) {
            uniqueId = uniqueIdProvider(Unit)
            child = PacketHandler(uniqueId!!, plugin, virtualHandlerRegistry)
        }
    }

    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        val uniqueId = uniqueId

        if (uniqueId != null && !first &&
            (message::class.simpleName == "ClientboundSetDefaultSpawnPositionPacket" || message::class.simpleName == "PacketPlayOutSpawnPosition")) {
            plugin as HQBukkitPlugin
            val player = plugin.server.getPlayer(uniqueId)
            if (player != null) {
                plugin.launch(Dispatchers.Default) {
                    val handler = (virtualHandlerRegistry as VirtualHandlerRegistryImpl).findLoadHandler()
                    runCatching {
                        handler?.invoke(uniqueId)
                    }.onFailure {
                        promise.tryFailureOnEventLoop(context, it)
                    }

                    first = true
                    context.ensureOnEventLoop {
                        super.write(context, message, promise)
                    }
                }
            } else super.write(context, message, promise)
        } else {
            val result = child?.write0(context, message, promise) ?: emptyList()
            if (result.isNotEmpty()) {
                result.forEach { super.write(context, it, promise) }
            } else super.write(context, message, promise)
        }
    }

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var child = child
        val result = if (child == null) {
            val uniqueId = uniqueIdProvider(message)
            if (uniqueId != null) {
                this.uniqueId = uniqueId
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

    inline fun ChannelHandlerContext.ensureOnEventLoop(crossinline command: () -> Unit) {
        if (executor().inEventLoop()) {
            command.invoke()
        } else {
            executor().execute { command.invoke() }
        }
    }

    fun ChannelPromise.trySuccessOnEventLoop(context: ChannelHandlerContext) {
        val executor = context.executor()
        if (executor.inEventLoop()) {
            trySuccess()
        } else {
            executor.execute { trySuccess() }
        }
    }

    fun ChannelPromise.tryFailureOnEventLoop(context: ChannelHandlerContext, throwable: Throwable) {
        val executor = context.executor()
        if (executor.inEventLoop()) {
            tryFailure(throwable)
        } else {
            executor.execute { tryFailure(throwable) }
        }
    }
}