package kr.hqservice.framework.nms.v21_11.service.netty

import io.netty.channel.Channel
import kr.hqservice.framework.nms.handler.PacketHandler
import kr.hqservice.framework.nms.util.NmsNettyInjectService
import kr.hqservice.framework.nms.v21_11.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class NmsNettyInjectServiceImpl(
    private val plugin: Plugin,
    private val reflectionWrapper: NmsReflectionWrapperImpl,
    private val virtualHandlerRegistry: VirtualHandlerRegistry
) : NmsNettyInjectService {
    override fun getPlayerChannel(player: Player): Channel {
        val entity = reflectionWrapper.getEntityPlayer(player)
        val listener = entity.connection
        val connection = listener.connection
        return connection.channel
    }

    override fun injectHandler(player: Player) {
        val channel = getPlayerChannel(player)
        val pipeline = channel.pipeline()

        if (pipeline.get("hq_packet_handler") == null) {
            pipeline.addBefore(
                "packet_handler",
                "hq_packet_handler",
                PacketHandler(player.uniqueId, plugin, virtualHandlerRegistry)
            )
        }
    }

    override fun removeHandler(player: Player) {
        val channel = getPlayerChannel(player)
        channel.eventLoop().submit {
            virtualHandlerRegistry.cleanup(player.uniqueId)
            channel.pipeline().remove("hq_packet_handler")
            return@submit Unit
        }
    }
}