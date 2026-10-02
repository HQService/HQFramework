package kr.hqservice.framework.bungee.core.netty.registry.impl

import kr.hqservice.framework.bungee.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.bungee.core.netty.event.NettyClientDisconnectedEvent
import kr.hqservice.framework.bungee.core.netty.event.NettyPacketReceivedEvent
import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.channel.ChannelConnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelListPacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.md_5.bungee.api.ProxyServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
@Singleton(binds = [NettyChannelRegistry::class])
class NettyChannelRegistryImpl(
    private val proxyServer: ProxyServer,
    private val config: HQYamlConfiguration
) : NettyChannelRegistry, HQSimpleComponent {
    private val portChannelContainer = ConcurrentHashMap<Int, ChannelWrapper>()
    private val nameChannelContainer = ConcurrentHashMap<String, ChannelWrapper>()
    private val unknownClientId = AtomicInteger(1)

    override fun registerActiveChannel(port: Int, wrapper: ChannelWrapper) {
        val name = proxyServer.servers.values.firstOrNull { it.address.port == port }?.name
            ?: "Unknown-${unknownClientId.getAndIncrement()}"
        evictPreviousChannel(port, wrapper)
        proxyServer.pluginManager.callEvent(
            NettyClientConnectedEvent(
                wrapper,
                wrapper.handler.connectionState,
                name
            )
        )

        val channelVO = NettyChannelImpl(port, name)
        val connectedPacket = ChannelConnectedPacket(channelVO)

        val connectedChannels = mutableListOf<NettyChannel>()
        connectedChannels.add(channelVO)
        nameChannelContainer.forEach { (channelName, wrap) ->
            connectedChannels.add(
                NettyChannelImpl(
                    wrap.port,
                    channelName
                )
            )
        }
        portChannelContainer.values.forEach { it.sendPacket(connectedPacket) }

        portChannelContainer[port] = wrapper
        nameChannelContainer[name] = wrapper

        val handlerBoss = wrapper.channel.pipeline().get(BossHandler::class.java)
        handlerBoss.setDisconnectionHandler(this::onChannelInactive)
        handlerBoss.setPacketPreprocessHandler { packet, wrap ->
            proxyServer.pluginManager.callEvent(NettyPacketReceivedEvent(packet, wrap))
        }

        Thread {
            val players = proxyServer.players.mapNotNullTo(mutableListOf<NettyPlayer>()) { player ->
                val serverPort = (player.server?.info?.socketAddress as? InetSocketAddress)?.port
                if (serverPort == null) {
                    proxyServer.logger.warning("skipping ${player.name} in channel list: not connected to a backend yet")
                    return@mapNotNullTo null
                }
                NettyPlayerImpl(
                    player.name,
                    player.displayName,
                    player.uniqueId,
                    connectedChannels.firstOrNull { channel -> channel.getPort() == serverPort }
                )
            }
            wrapper.sendPacket(ChannelListPacket(connectedChannels, players))
        }.start()
    }

    private fun evictPreviousChannel(port: Int, wrapper: ChannelWrapper) {
        val previous = portChannelContainer[port] ?: return
        if (previous === wrapper) return
        portChannelContainer.remove(port, previous)
        nameChannelContainer.entries.removeIf { it.value === previous }
        previous.channel.close()
    }

    override fun loopChannels(block: (ChannelWrapper) -> Unit) {
        portChannelContainer.values.forEach(block)
    }

    override fun getChannels(): List<ChannelWrapper> {
        return portChannelContainer.values.toList()
    }

    override fun getChannelNameByPort(port: Int): String {
        return nameChannelContainer.entries.firstOrNull { it.value.port == port }?.key
            ?: "Unknown-$port"
    }

    private fun onChannelInactive(wrapper: ChannelWrapper) {
        val name = nameChannelContainer.entries.firstOrNull { it.value === wrapper }?.key
        if (!portChannelContainer.remove(wrapper.port, wrapper)) return
        if (name != null) nameChannelContainer.remove(name, wrapper)

        proxyServer.pluginManager.callEvent(
            NettyClientDisconnectedEvent(
                wrapper,
                wrapper.handler.connectionState
            )
        )

        val channelVO = NettyChannelImpl(wrapper.port, name ?: "Unknown-${wrapper.port}")
        val packet = ChannelDisconnectedPacket(channelVO)
        portChannelContainer.values.forEach { it.sendPacket(packet) }
    }

    override fun shutdown() {
        portChannelContainer.values.forEach {
            it.channel.writeAndFlush(ShutdownPacket(config.getBoolean("netty.shutdown-servers")))
            if (it.channel.isActive && it.channel.isOpen)
                it.channel.close()
        }
    }

    override fun getChannelByPort(port: Int): ChannelWrapper {
        return portChannelContainer[port] ?: throw IllegalArgumentException()
    }

    override fun getChannelByServerName(name: String): ChannelWrapper {
        return nameChannelContainer[name] ?: throw IllegalArgumentException()
    }

    override fun forEachChannels(block: (ChannelWrapper) -> Unit) {
        portChannelContainer.values.forEach(block)
    }
}