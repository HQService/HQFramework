package kr.hqservice.framework.bungee.core.netty.api

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.netty.api.HQNettyAPI
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.PacketHandler
import kotlin.reflect.KClass

@Component
@HQSingleton(binds = [HQNettyAPI::class])
class ProxyNettyAPI(
    private val packetSender: PacketSender
) : HQNettyAPI, HQService {
    override fun getPacketSender(): PacketSender {
        return packetSender
    }

    override fun <T : Packet> registerOuterPacket(packetClass: KClass<T>) {
        Direction.OUTBOUND.registerPacket(packetClass)
    }

    override fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: (packet: T, channel: ChannelWrapper) -> Unit,
    ) {
        Direction.INBOUND.registerPacket(packetClass)
        Direction.INBOUND.addListener(packetClass, packetHandler)
    }

    override fun <T : Packet> registerInnerPacket(packetClass: KClass<T>, packetHandler: PacketHandler<T>) {
        Direction.INBOUND.registerPacket(packetClass)
        Direction.INBOUND.addListener(packetClass, packetHandler)
    }
}