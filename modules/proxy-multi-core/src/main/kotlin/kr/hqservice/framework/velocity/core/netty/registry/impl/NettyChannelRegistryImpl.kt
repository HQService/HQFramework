package kr.hqservice.framework.velocity.core.netty.registry.impl

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import com.velocitypowered.api.proxy.ProxyServer
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
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.velocity.core.netty.event.NettyClientDisconnectedEvent
import kr.hqservice.framework.velocity.core.netty.event.NettyPacketReceivedEvent
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.concurrent.ConcurrentHashMap

@Component
@Singleton(binds = [NettyChannelRegistry::class])
class NettyChannelRegistryImpl(
    private val plugin: HQVelocityPlugin,
    private val config: HQYamlConfiguration,
) : NettyChannelRegistry, HQSimpleComponent {
    private val server = plugin.getProxyServer()
    private val portChannelContainer = ConcurrentHashMap<Int, ChannelWrapper>()
    private val nameChannelContainer = ConcurrentHashMap<String, ChannelWrapper>()
    private val unknownClientId = java.util.concurrent.atomic.AtomicInteger(1)

    override fun registerActiveChannel(port: Int, wrapper: ChannelWrapper) {
        val name = server.allServers.firstOrNull { it.serverInfo.address.port == port }?.serverInfo?.name
            ?: "Unknown-${unknownClientId.getAndIncrement()}"

        portChannelContainer[port] = wrapper
        nameChannelContainer[name] = wrapper

        server.scheduler.buildTask(plugin, Runnable {
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
        }).schedule()

        val channelVO = NettyChannelImpl(port, name)
        val connectedPacket = ChannelConnectedPacket(channelVO)

        val connectedChannels = mutableListOf<NettyChannel>().apply {
            add(channelVO)
            nameChannelContainer.forEach { (channelName, wrap) ->
                if (wrap !== wrapper) {
                    add(NettyChannelImpl(wrap.port, channelName))
                }
            }
        }

        portChannelContainer.values.forEach { ch ->
            ch.channel.eventLoop().execute {
                if (ch.channel.isActive) ch.sendPacket(connectedPacket)
            }
        }

        val handlerBoss = wrapper.channel.pipeline().get(BossHandler::class.java)
        handlerBoss.setDisconnectionHandler(this::onChannelInactive)
        handlerBoss.setPacketPreprocessHandler { packet, wrap ->
            server.scheduler.buildTask(plugin, Runnable {
                try {
                    server.eventManager.fire(NettyPacketReceivedEvent(packet, wrap))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).schedule()
        }

        server.scheduler.buildTask(plugin, Runnable {
            val players = mutableListOf<NettyPlayer>()
            val redisAbs = RedisBungeeAPI.getAbstractRedisBungeeAPI()
            runCatching {
                redisAbs.serverToPlayers.entries().forEach {
                    try {
                        players.add(
                            NettyPlayerImpl(
                                redisAbs.getNameFromUuid(it.value),
                                redisAbs.getNameFromUuid(it.value),
                                it.value,
                                connectedChannels.firstOrNull { channel -> channel.getName() == it.key }
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            wrapper.channel.eventLoop().execute {
                if (wrapper.channel.isActive) {
                    wrapper.sendPacket(ChannelListPacket(connectedChannels, players))
                }
            }
        }).schedule()
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
        server.scheduler.buildTask(plugin, Runnable {
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
        }).schedule()

        runCatching {
            val name = getChannelNameByPort(wrapper.port)
            portChannelContainer.remove(wrapper.port)
            nameChannelContainer.remove(name)
            val channelVO = NettyChannelImpl(wrapper.port, name)
            val packet = ChannelDisconnectedPacket(channelVO)
            portChannelContainer.values.forEach { ch ->
                ch.channel.eventLoop().execute {
                    if (ch.channel.isActive) ch.sendPacket(packet)
                }
            }
        }
    }

    override fun shutdown() {
        portChannelContainer.values.forEach { cw ->
            cw.channel.eventLoop().execute {
                cw.channel.writeAndFlush(ShutdownPacket(config.getBoolean("netty.shutdown-servers"))).addListener { _ ->
                    if (cw.channel.isActive || cw.channel.isOpen) {
                        cw.channel.close()
                    }
                }
            }
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