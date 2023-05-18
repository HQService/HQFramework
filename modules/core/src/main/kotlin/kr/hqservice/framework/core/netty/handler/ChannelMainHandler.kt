package kr.hqservice.framework.core.netty.handler

import kr.hqservice.framework.core.netty.event.NettyClientConnectedEvent
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.PacketHandler
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.bukkit.plugin.Plugin

class ChannelMainHandler(
    private val plugin: Plugin
) : PacketHandler<HandShakePacket> {
    private var channel: ChannelWrapper? = null

    override fun onPacketReceive(packet: HandShakePacket, channel: ChannelWrapper) {
        channel.handler.setConnectionState(ConnectionState.CONNECTED)
        this.channel = channel.handler.channel
        plugin.server.scheduler.runTask(plugin, Runnable { plugin.server.pluginManager.callEvent(NettyClientConnectedEvent(channel)) })
    }

    fun getChannel(): ChannelWrapper? {
        return channel
    }

    fun disconnect() {
        if(channel?.channel?.isOpen == true && channel?.channel?.isActive == true) {
            channel?.channel?.disconnect()
            channel = null
        }
    }
}