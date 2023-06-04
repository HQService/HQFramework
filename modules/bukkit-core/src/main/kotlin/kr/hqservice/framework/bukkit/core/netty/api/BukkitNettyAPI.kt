package kr.hqservice.framework.bukkit.core.netty.api

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.netty.api.HQNettyAPI
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.container.ChannelContainer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.PacketHandler
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import java.util.*
import kotlin.reflect.KClass

@Component
@HQSingleton(binds = [HQNettyAPI::class])
class BukkitNettyAPI(
    private val packetSender: PacketSender,
    private val container: ChannelContainer
) : HQNettyAPI, HQService {
    override fun getPacketSender(): PacketSender {
        return packetSender
    }

    override fun getChannels(): List<NettyChannel> {
        return container.getChannels()
    }

    override fun broadcast(message: String, logging: Boolean) {
        packetSender.sendPacketToProxy(BroadcastPacket(message, logging, null))
    }

    override fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean) {
        packetSender.sendPacketToProxy(BroadcastPacket(message, logging, channel))
    }

    override fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean) {
        packetSender.sendPacketToProxy(MessagePacket(message, logging, players))
    }

    override fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean) {
        packetSender.sendPacketToProxy(MessagePacket(message, logging, listOf(player)))
    }

    override fun getChannel(name: String): NettyChannel? {
        return getChannels().firstOrNull { it.getName() == name }
    }

    override fun getChannel(port: Int): NettyChannel? {
        return getChannels().firstOrNull { it.getPort() == port }
    }

    override fun getPlayer(name: String): NettyPlayer? {
        return getPlayers().firstOrNull { it.getName() == name }
    }

    override fun getPlayer(uniqueId: UUID): NettyPlayer? {
        return getPlayers().firstOrNull { it.getUniqueId() == uniqueId }
    }

    override fun getPlayers(): List<NettyPlayer> {
        return container.getPlayers()
    }

    override fun getPlayers(channel: NettyChannel): List<NettyPlayer> {
        return container.getPlayers(channel)
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