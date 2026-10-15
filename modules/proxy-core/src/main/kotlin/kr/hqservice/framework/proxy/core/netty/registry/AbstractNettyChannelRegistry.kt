package kr.hqservice.framework.proxy.core.netty.registry

import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.channel.ChannelConnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelListPacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractNettyChannelRegistry(
    private val config: HQYamlConfiguration
) : NettyChannelRegistry {
    private val portChannelContainer = ConcurrentHashMap<Int, ChannelWrapper>()
    private val nameChannelContainer = ConcurrentHashMap<String, ChannelWrapper>()
    private val unknownClientId = AtomicInteger(1)

    protected open val registersBeforeAnnouncing: Boolean = false

    protected abstract fun resolveServerName(port: Int): String?

    protected abstract fun fireConnected(wrapper: ChannelWrapper, name: String)

    protected abstract fun fireDisconnected(wrapper: ChannelWrapper)

    protected abstract fun firePacketReceived(packet: Packet, wrapper: ChannelWrapper)

    protected abstract fun runAsync(task: () -> Unit)

    protected abstract fun deliver(wrapper: ChannelWrapper, packet: Packet)

    protected abstract fun sendShutdown(wrapper: ChannelWrapper, packet: ShutdownPacket)

    protected abstract fun collectPlayers(connectedChannels: List<NettyChannel>): MutableList<NettyPlayer>

    override fun registerActiveChannel(port: Int, wrapper: ChannelWrapper) {
        val name = resolveServerName(port) ?: "Unknown-${unknownClientId.getAndIncrement()}"
        evictPreviousChannel(port, wrapper)
        if (registersBeforeAnnouncing) store(port, name, wrapper)
        fireConnected(wrapper, name)

        val channelVO = NettyChannelImpl(port, name)
        val connectedPacket = ChannelConnectedPacket(channelVO)
        val connectedChannels = mutableListOf<NettyChannel>(channelVO)
        nameChannelContainer.forEach { (channelName, wrap) ->
            if (!registersBeforeAnnouncing || wrap !== wrapper) {
                connectedChannels.add(NettyChannelImpl(wrap.port, channelName))
            }
        }
        portChannelContainer.values.forEach { deliver(it, connectedPacket) }
        if (!registersBeforeAnnouncing) store(port, name, wrapper)

        val handlerBoss = wrapper.channel.pipeline().get(BossHandler::class.java)
        handlerBoss.setDisconnectionHandler(this::onChannelInactive)
        handlerBoss.setPacketPreprocessHandler(this::firePacketReceived)

        runAsync {
            val players = collectPlayers(connectedChannels)
            deliver(wrapper, ChannelListPacket(connectedChannels, players))
        }
    }

    private fun store(port: Int, name: String, wrapper: ChannelWrapper) {
        portChannelContainer[port] = wrapper
        nameChannelContainer[name] = wrapper
    }

    private fun evictPreviousChannel(port: Int, wrapper: ChannelWrapper) {
        val previous = portChannelContainer[port] ?: return
        if (previous === wrapper) return
        portChannelContainer.remove(port, previous)
        nameChannelContainer.entries.removeIf { it.value === previous }
        previous.channel.close()
    }

    private fun onChannelInactive(wrapper: ChannelWrapper) {
        val name = nameChannelContainer.entries.firstOrNull { it.value === wrapper }?.key
        if (!portChannelContainer.remove(wrapper.port, wrapper)) return
        if (name != null) nameChannelContainer.remove(name, wrapper)

        fireDisconnected(wrapper)

        val channelVO = NettyChannelImpl(wrapper.port, name ?: "Unknown-${wrapper.port}")
        val packet = ChannelDisconnectedPacket(channelVO)
        portChannelContainer.values.forEach { deliver(it, packet) }
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

    override fun shutdown() {
        portChannelContainer.values.forEach {
            sendShutdown(it, ShutdownPacket(config.getBoolean("netty.shutdown-servers")))
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
