package kr.hqservice.framework.bukkit.core.netty.handler.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.bukkit.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.bukkit.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.bukkit.plugin.Plugin
import org.koin.core.annotation.Named

@Component
@HQSingleton(binds = [ChannelMainHandler::class, PacketSender::class])
class ChannelMainHandlerImpl(
    @Named("hqframework") private val plugin: Plugin
) : ChannelMainHandler {
    private var proxyChannel: ChannelWrapper? = null

    override fun onPacketReceive(packet: HandShakePacket, channel: ChannelWrapper) {
        channel.handler.setConnectionState(ConnectionState.CONNECTED)
        this.proxyChannel = channel.handler.channel
        plugin.server.scheduler.runTask(plugin, Runnable { plugin.server.pluginManager.callEvent(
            NettyClientConnectedEvent(channel)
        ) })
    }

    override fun sendPacketToProxy(packet: Packet) {
        proxyChannel?.sendPacket(packet)
    }

    override fun sendPacketAll(packet: Packet) {
        proxyChannel?.sendPacket(RelayingPacket(packet))
    }

    override fun sendPacket(port: Int, packet: Packet) {
        proxyChannel?.sendPacket(RelayingPacket(packet, port.toString()))
    }

    override fun sendPacket(name: String, packet: Packet) {
        proxyChannel?.sendPacket(RelayingPacket(packet, name))
    }

    override fun disconnect() {
        if(proxyChannel?.channel?.isOpen == true && proxyChannel?.channel?.isActive == true) {
            proxyChannel?.channel?.disconnect()
            proxyChannel = null
        }
    }
}