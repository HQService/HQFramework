package kr.hqservice.framework.nms.util.impl

import io.netty.channel.Channel
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.PacketHandler
import kr.hqservice.framework.nms.util.NettyInjectUtil
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.Collections
import java.util.LinkedList
import kotlin.reflect.jvm.isAccessible

@Component
@HQSingleton(binds = [NettyInjectUtil::class])
class NettyInjectUtilImpl(
    private val reflectionWrapper: NmsReflectionWrapper
) : NettyInjectUtil, HQSimpleComponent {
    override fun getPlayerChannel(player: Player): Channel {
        val entity = reflectionWrapper.getEntityPlayer(player)
        val listenerClass = reflectionWrapper.getNmsClass("PlayerConnection", Version.V_15.handle("server.network.ServerGamePacketListenerImpl", true))
        val listener = reflectionWrapper.getField(entity::class, listenerClass).call(entity)

        val connectionClass = reflectionWrapper.getNmsClass("NetworkManager", Version.V_15.handle("network"))
        val connectionField = reflectionWrapper.getField(listenerClass, connectionClass)
        val connection = connectionField.call(listener)

        val channelField = reflectionWrapper.getField(connectionClass, Channel::class)
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

    override fun injectHandler(player: Player, channel: Channel) {
        val pipeline = channel.pipeline()

        if(pipeline.get("hq_injector") == null)
            pipeline.addBefore("packet_handler", "hq_injector", PacketHandler(player))
    }

    override fun removeHandler(channel: Channel) {
        val pipeline = channel.pipeline()

        if(pipeline.get("hq_injector") != null)
            pipeline.remove("hq_injector")
    }
}