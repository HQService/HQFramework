package kr.hqservice.framework.nms.hook

import io.netty.channel.Channel
import kr.hqservice.framework.nms.handler.EarlyPacketHandler
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import java.util.UUID

object EarlyHookInstaller {
    const val HANDLER_NAME = "hq_packet_handler"
    private const val VANILLA_HANDLER_NAME = "packet_handler"
    val LISTENER_KEY: Key = Key.key("hqservice:early-pipeline-hook")

    @Volatile
    private var uninstallAction: (() -> Unit)? = null

    fun install(
        plugin: Plugin,
        registry: VirtualHandlerRegistry,
        addListener: (Key, (Channel) -> Unit) -> Unit,
        removeListener: (Key) -> Unit,
        uniqueIdProvider: (Any) -> UUID?
    ) {
        addListener(LISTENER_KEY) { channel ->
            channel.eventLoop().execute {
                val pipeline = channel.pipeline()
                if (pipeline.get(HANDLER_NAME) == null && pipeline.get(VANILLA_HANDLER_NAME) != null) {
                    pipeline.addBefore(
                        VANILLA_HANDLER_NAME, HANDLER_NAME,
                        EarlyPacketHandler(plugin, registry, uniqueIdProvider = uniqueIdProvider)
                    )
                }
            }
        }
        uninstallAction = { removeListener(LISTENER_KEY) }
    }

    fun uninstall() {
        uninstallAction?.invoke()
        uninstallAction = null
    }

    fun bindUniqueId(channel: Channel, uniqueId: UUID) {
        channel.eventLoop().execute {
            (channel.pipeline().get(HANDLER_NAME) as? EarlyPacketHandler)?.bindUniqueId(uniqueId)
        }
    }
}
