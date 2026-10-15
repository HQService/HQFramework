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
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.proxy.core.netty.registry.AbstractNettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.md_5.bungee.api.ProxyServer
import java.net.InetSocketAddress

@Component
@Singleton(binds = [NettyChannelRegistry::class])
class NettyChannelRegistryImpl(
    private val proxyServer: ProxyServer,
    config: HQYamlConfiguration
) : AbstractNettyChannelRegistry(config), NettyChannelRegistry, HQSimpleComponent {
    override fun resolveServerName(port: Int): String? {
        return proxyServer.servers.values.firstOrNull { it.address.port == port }?.name
    }

    override fun fireConnected(wrapper: ChannelWrapper, name: String) {
        proxyServer.pluginManager.callEvent(
            NettyClientConnectedEvent(
                wrapper,
                wrapper.handler.connectionState,
                name
            )
        )
    }

    override fun fireDisconnected(wrapper: ChannelWrapper) {
        proxyServer.pluginManager.callEvent(
            NettyClientDisconnectedEvent(
                wrapper,
                wrapper.handler.connectionState
            )
        )
    }

    override fun firePacketReceived(packet: Packet, wrapper: ChannelWrapper) {
        proxyServer.pluginManager.callEvent(NettyPacketReceivedEvent(packet, wrapper))
    }

    override fun runAsync(task: () -> Unit) {
        Thread(task).start()
    }

    override fun deliver(wrapper: ChannelWrapper, packet: Packet) {
        wrapper.sendPacket(packet)
    }

    override fun sendShutdown(wrapper: ChannelWrapper, packet: ShutdownPacket) {
        wrapper.channel.writeAndFlush(packet)
        if (wrapper.channel.isActive && wrapper.channel.isOpen)
            wrapper.channel.close()
    }

    override fun collectPlayers(connectedChannels: List<NettyChannel>): MutableList<NettyPlayer> {
        return proxyServer.players.mapNotNullTo(mutableListOf()) { player ->
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
    }
}
