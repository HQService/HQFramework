package kr.hqservice.framework.nms.util.impl

import io.netty.channel.Channel
import io.netty.channel.ChannelPipeline
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.PacketHandler
import kr.hqservice.framework.nms.util.NettyInjectUtil
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.Collections
import java.util.LinkedList
import java.util.UUID
import kotlin.reflect.jvm.isAccessible

@Component
@HQSingleton(binds = [NettyInjectUtil::class])
class NettyInjectUtilImpl(
    private val plugin: Plugin,
    private val reflectionWrapper: NmsReflectionWrapper,
    private val virtualHandlerRegistry: VirtualHandlerRegistry
) : NettyInjectUtil, HQSimpleComponent {
    private val listenerClass = reflectionWrapper.getNmsClass("PlayerConnection", Version.V_15.handle("server.network.ServerGamePacketListenerImpl", true))
    private val connectionClass = reflectionWrapper.getNmsClass("NetworkManager", Version.V_15.handle("network"))
    private val connectionField = reflectionWrapper.getField(listenerClass, connectionClass)
    private val channelField = reflectionWrapper.getField(connectionClass, Channel::class)

    override fun getPlayerChannel(player: Player): Channel {
        val entity = reflectionWrapper.getEntityPlayer(player)
        val listener = reflectionWrapper.getField(entity::class, listenerClass).call(entity)
        val connection = connectionField.call(listener)

        channelField.isAccessible = true
        return channelField.call(connection) as Channel
    }

    override fun getServerChannels(server: Server): List<Channel> {
        val nmsServer = reflectionWrapper.getNmsServer(server)
        val mcServerClass = reflectionWrapper.getNmsClass("MinecraftServer", Version.V_15.handle("server"))

        val serverConnectionListener = reflectionWrapper.getNmsClass("ServerConnection", Version.V_15.handle("server.network"))
        val listenerField = reflectionWrapper.getField(mcServerClass, serverConnectionListener)
        listenerField.isAccessible = true
        val listener = listenerField.call(nmsServer)

        val connectionField = reflectionWrapper.getField(serverConnectionListener, "h", Version.V_15.handle("g"))
        connectionField.isAccessible = true

        val connectionType = reflectionWrapper.getNmsClass("NetworkManager", Version.V_15.handle("network"))
        val connections = connectionField.call(listener) as List<*>

        val output = LinkedList<Channel>()
        val channelField = reflectionWrapper.getField(connectionType, Channel::class)

        connections.forEach {
            output.add(channelField.call(it) as Channel)
        }

        return Collections.unmodifiableList(output)
    }

    override fun injectHandler(player: Player) {
        val channel = getPlayerChannel(player)
        val pipeline = channel.pipeline()

        if(pipeline.get("hq_injector") == null) {
            pipeline.addBefore("packet_handler", "hq_injector", PacketHandler(player, plugin, virtualHandlerRegistry))
        }
    }

    override fun removeHandler(player: Player) {
        val pipeline = getPlayerChannel(player).pipeline()
        if(pipeline.get("hq_injector") != null) {
            virtualHandlerRegistry.cleanup(player.uniqueId)
            pipeline.remove("hq_injector")
        }
    }
}