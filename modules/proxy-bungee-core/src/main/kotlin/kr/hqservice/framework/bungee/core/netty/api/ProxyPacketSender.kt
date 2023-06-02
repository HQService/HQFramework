package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Packet

@Component
@HQSingleton(binds = [PacketSender::class])
class ProxyPacketSender(
    private val channelContainer: NettyChannelRegistry
) : PacketSender, HQService {
    override fun sendPacketToProxy(packet: Packet) {
        throw UnsupportedOperationException("proxy to proxy")
    }

    override fun sendPacketAll(packet: Packet) {
        channelContainer.loopChannels {
            it.sendPacket(packet)
        }
    }

    override fun sendPacket(port: Int, packet: Packet) {
        channelContainer.getChannelByPort(port).sendPacket(packet)
    }

    override fun sendPacket(name: String, packet: Packet) {
        channelContainer.getChannelByServerName(name).sendPacket(packet)
    }
}