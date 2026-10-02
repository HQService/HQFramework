package kr.hqservice.framework.velocity.core.netty.registry.impl

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
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.velocity.core.netty.event.NettyClientDisconnectedEvent
import kr.hqservice.framework.velocity.core.netty.event.NettyPacketReceivedEvent
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kotlin.jvm.optionals.getOrNull

@Component
@Singleton(binds = [NettyChannelRegistry::class])
open class NettyChannelRegistryImpl(
    private val plugin: HQVelocityPlugin,
    config: HQYamlConfiguration
) : AbstractNettyChannelRegistry(config), NettyChannelRegistry, HQSimpleComponent {
    private val server = plugin.getProxyServer()

    override val registersBeforeAnnouncing: Boolean = true

    override fun resolveServerName(port: Int): String? {
        return server.allServers.firstOrNull { it.serverInfo.address.port == port }?.serverInfo?.name
    }

    override fun fireConnected(wrapper: ChannelWrapper, name: String) {
        runAsync {
            try {
                server.eventManager.fire(
                    NettyClientConnectedEvent(
                        wrapper,
                        wrapper.handler.connectionState,
                        name
                    )
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun fireDisconnected(wrapper: ChannelWrapper) {
        runAsync {
            try {
                server.eventManager.fire(
                    NettyClientDisconnectedEvent(
                        wrapper,
                        wrapper.handler.connectionState
                    )
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun firePacketReceived(packet: Packet, wrapper: ChannelWrapper) {
        runAsync {
            try {
                server.eventManager.fire(NettyPacketReceivedEvent(packet, wrapper))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun runAsync(task: () -> Unit) {
        server.scheduler.buildTask(plugin, Runnable(task)).schedule()
    }

    override fun deliver(wrapper: ChannelWrapper, packet: Packet) {
        wrapper.channel.eventLoop().execute {
            if (wrapper.channel.isActive) wrapper.sendPacket(packet)
        }
    }

    override fun sendShutdown(wrapper: ChannelWrapper, packet: ShutdownPacket) {
        wrapper.channel.eventLoop().execute {
            wrapper.channel.writeAndFlush(packet).addListener { _ ->
                if (wrapper.channel.isActive || wrapper.channel.isOpen) {
                    wrapper.channel.close()
                }
            }
        }
    }

    override fun collectPlayers(connectedChannels: List<NettyChannel>): MutableList<NettyPlayer> {
        return server.allPlayers.mapTo(mutableListOf()) {
            NettyPlayerImpl(
                it.username,
                it.username,
                it.uniqueId,
                connectedChannels.firstOrNull { channel -> channel.getPort() == it.currentServer.getOrNull()?.serverInfo?.address?.port }
            )
        }
    }
}
