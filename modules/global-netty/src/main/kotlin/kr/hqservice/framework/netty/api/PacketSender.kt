package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.packet.Packet
import net.md_5.bungee.api.chat.BaseComponent

interface PacketSender {
    fun sendPacketToProxy(packet: Packet)

    fun sendPacketAll(packet: Packet)

    fun sendPacket(port: Int, packet: Packet)

    fun sendPacket(name: String, packet: Packet)

    @Deprecated("use the BaseComponent overload")
    fun broadcast(message: String, logging: Boolean)

    fun broadcast(message: BaseComponent, logging: Boolean)

    @Deprecated("use the BaseComponent overload")
    fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean)

    fun sendMessageToChannel(channel: NettyChannel, message: BaseComponent, logging: Boolean)

    @Deprecated("use the BaseComponent overload")
    fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean)

    @Deprecated("use the BaseComponent overload")
    fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean)

    fun sendMessageToPlayers(players: List<NettyPlayer>, message: BaseComponent, logging: Boolean)

    fun sendMessageToPlayer(player: NettyPlayer, message: BaseComponent, logging: Boolean)
}