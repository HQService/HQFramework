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

@Component
@Singleton(binds = [NettyChannelRegistry::class])
class NettyChannelRegistryImpl(
    private val proxyServer: ProxyServer,
    private val config: HQYamlConfiguration
) : NettyChannelRegistry, HQSimpleComponent {
    private val portChannelContainer = mutableMapOf<Int, ChannelWrapper>()
    private val nameChannelContainer = mutableMapOf<String, ChannelWrapper>()
    private var unknownClientId = 1

    override fun registerActiveChannel(port: Int, wrapper: ChannelWrapper) {
        val name = ProxyServer.getInstance().servers.values.firstOrNull { it.address.port == port }?.name
            ?: "Unknown-${unknownClientId++}"
        ProxyServer.getInstance().pluginManager.callEvent(
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
            ProxyServer.getInstance().pluginManager.callEvent(NettyPacketReceivedEvent(packet, wrap))
        }

        Thread {
            val players = mutableListOf<NettyPlayer>()
            ProxyServer.getInstance().players.forEach {
                try {
                    players.add(
                        NettyPlayerImpl(
                            it.name,
                            it.displayName,
                            it.uniqueId,
                            connectedChannels.firstOrNull { channel -> channel.getPort() == it.server.address.port })
                    )
                } catch (e: Exception) {
                    runCatching {
                        proxyServer.servers["lobby"]?.let { ch ->
                            it.connect(ch)
                        }
                    }.onFailure { exception ->
                        e.printStackTrace()
                        //it.disconnect("§c서버가 로드중입니다.\n§c잠시 후 다시 접속해주세요!")
                    }
                }
            }
            wrapper.sendPacket(ChannelListPacket(connectedChannels, players))
        }.start()
    }

    override fun loopChannels(block: (ChannelWrapper) -> Unit) {
        portChannelContainer.values.forEach(block)
    }

    override fun getChannels(): List<ChannelWrapper> {
        return portChannelContainer.values.toList()
    }

    override fun getChannelNameByPort(port: Int): String {
        return nameChannelContainer.entries.firstOrNull { it.value.port == port }?.key
            ?: throw IllegalArgumentException()
    }

    private fun onChannelInactive(wrapper: ChannelWrapper) {
        ProxyServer.getInstance().pluginManager.callEvent(
            NettyClientDisconnectedEvent(
                wrapper,
                wrapper.handler.connectionState
            )
        )

        val name = getChannelNameByPort(wrapper.port)
        portChannelContainer.remove(wrapper.port)
        nameChannelContainer.remove(name)
        val channelVO = NettyChannelImpl(wrapper.port, name)
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