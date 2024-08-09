package kr.hqservice.framework.nms.util.impl

import io.netty.channel.Channel
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.handler.PacketHandler
import kr.hqservice.framework.nms.util.NettyInjectUtil
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

@Component
@Singleton(binds = [NettyInjectUtil::class])
class NettyInjectUtilImpl(
    private val plugin: Plugin,
    private val reflectionWrapper: NmsReflectionWrapper,
    private val virtualHandlerRegistry: VirtualHandlerRegistry
) : NettyInjectUtil, HQSimpleComponent {
    override fun getPlayerChannel(player: Player): Channel {
        val entity = reflectionWrapper.getEntityPlayer(player)
        val listener = entity.connection
        val connection = listener.connection
        return connection.channel
    }

    override fun getServerChannels(server: Server): List<Channel> {
        val nmsServer = reflectionWrapper.getNmsServer(server)
        val listener = nmsServer.connection
        val connections = listener.connections

        val output = LinkedList<Channel>()

        connections.forEach {
            output.add(it.channel)
        }

        return Collections.unmodifiableList(output)
    }

    override fun injectHandler(player: Player) {
        val channel = getPlayerChannel(player)
        val pipeline = channel.pipeline()

        if (pipeline.get("hq_packet_handler") == null) {
            pipeline.addBefore(
                "packet_handler",
                "hq_packet_handler",
                PacketHandler(player, plugin, virtualHandlerRegistry)
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