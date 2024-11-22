package kr.hqservice.framework.velocity.core.netty.registry.impl

import kr.hqservice.framework.velocity.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.velocity.core.netty.event.NettyClientDisconnectedEvent
import kr.hqservice.framework.velocity.core.netty.event.NettyPacketReceivedEvent
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
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
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.concurrent.ConcurrentHashMap

@Component
@Singleton(binds = [NettyChannelRegistry::class])
class NettyChannelRegistryImpl(
    private val plugin: HQVelocityPlugin,
    private val config: HQYamlConfiguration
) : NettyChannelRegistry, HQSimpleComponent {
    private val server = plugin.getProxyServer()
    private val portChannelContainer = ConcurrentHashMap<Int, ChannelWrapper>()
    private val nameChannelContainer = ConcurrentHashMap<String, ChannelWrapper>()
    private var unknownClientId = 1

    override fun registerActiveChannel(port: Int, wrapper: ChannelWrapper) {
        val name = server.allServers.firstOrNull { it.serverInfo.address.port == port }?.serverInfo?.name
            ?: "Unknown-${unknownClientId++}"
        server.eventManager.fire(NettyClientConnectedEvent(
            wrapper,
            wrapper.handler.connectionState,
            name
        ))

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
            server.eventManager.fire(NettyPacketReceivedEvent(packet, wrap))
        }

        Thread {
            val players = mutableListOf<NettyPlayer>()
            server.allPlayers.forEach {
                try {
                    players.add(
                        NettyPlayerImpl(
                            it.username,
                            it.username,
                            it.uniqueId,
                            connectedChannels.firstOrNull { channel -> channel.getPort() == it.currentServer.get().serverInfo.address.port })
                    )
                } catch (_: Exception) {
                    it.disconnect(LegacyComponentSerializer.legacySection().deserialize("§c서버가 로드중입니다.\n§c잠시 후 다시 접속해주세요!"))
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
        server.eventManager.fire(
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