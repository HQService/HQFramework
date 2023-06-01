package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.bungee.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.bungee.core.netty.event.NettyClientDisconnectedEvent
import kr.hqservice.framework.bungee.core.netty.event.NettyPacketReceivedEvent
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import net.md_5.bungee.api.ProxyServer

class NettyChannelContainer(
    private val shutdownServers: Boolean,
) {
    private val portChannelContainer = mutableMapOf<Int, ChannelWrapper>()
    private val nameChannelContainer = mutableMapOf<String, ChannelWrapper>()
    private var unknownClientId = 1

    fun onChannelActive(port: Int, wrapper: ChannelWrapper) {
        val name = ProxyServer.getInstance().servers.values.firstOrNull { it.address.port == port }?.name
            ?: "Unknown-${unknownClientId++}"
        ProxyServer.getInstance().pluginManager.callEvent(
            NettyClientConnectedEvent(
                wrapper,
                wrapper.handler.connectionState,
                name
            )
        )
        portChannelContainer[port] = wrapper
        nameChannelContainer[name] = wrapper

        val handlerBoss = wrapper.channel.pipeline().get(BossHandler::class.java)
        handlerBoss.setDisconnectionHandler(this::onChannelInactive)
        handlerBoss.setPacketPreprocessHandler { packet, wrap ->
            ProxyServer.getInstance().pluginManager.callEvent(NettyPacketReceivedEvent(packet, wrap))
        }
    }

    fun getChannelNameByPort(port: Int): String {
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
        portChannelContainer.entries.removeIf { it.value == wrapper }
        nameChannelContainer.entries.removeIf { it.value == wrapper }
    }

    fun shutdown() {
        portChannelContainer.values.forEach {
            it.channel.writeAndFlush(ShutdownPacket(shutdownServers))
            if (it.channel.isActive && it.channel.isOpen)
                it.channel.close()
        }
    }

    fun getChannelByPort(port: Int): ChannelWrapper {
        return portChannelContainer[port] ?: throw IllegalArgumentException()
    }

    fun getChannelByServerName(name: String): ChannelWrapper {
        return nameChannelContainer[name] ?: throw IllegalArgumentException()
    }

    fun forEachChannels(block: (ChannelWrapper) -> Unit) {
        portChannelContainer.values.forEach(block)
    }
}