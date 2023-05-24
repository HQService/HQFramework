package kr.hqservice.framework.core.netty.handler.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.core.netty.handler.PacketSender
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.bukkit.plugin.Plugin
import org.koin.core.annotation.Named

@Component
@Singleton(binds = [ChannelMainHandler::class, PacketSender::class])
class ChannelMainHandlerImpl(
    @Named("hqframework") private val plugin: Plugin
) : ChannelMainHandler {
    private var proxyChannel: ChannelWrapper? = null

    override fun onPacketReceive(packet: HandShakePacket, channel: ChannelWrapper) {
        channel.handler.setConnectionState(ConnectionState.CONNECTED)
        this.proxyChannel = channel.handler.channel
        plugin.server.scheduler.runTask(plugin, Runnable { plugin.server.pluginManager.callEvent(NettyClientConnectedEvent(channel)) })
    }

    override fun sendPacketToProxy(packet: AbstractPacket) {
        proxyChannel?.sendPacket(packet)
    }

    override fun sendPacketAll(packet: AbstractPacket) {
        proxyChannel?.sendPacket(RelayingPacket(packet))
    }

    override fun sendPacket(port: Int, packet: AbstractPacket) {
        proxyChannel?.sendPacket(RelayingPacket(port.toString(), packet))
    }

    override fun sendPacket(name: String, packet: AbstractPacket) {
        proxyChannel?.sendPacket(RelayingPacket(name, packet))
    }

    override fun disconnect() {
        if(proxyChannel?.channel?.isOpen == true && proxyChannel?.channel?.isActive == true) {
            proxyChannel?.channel?.disconnect()
            proxyChannel = null
        }
    }
}