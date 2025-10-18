package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.packet.Packet
import net.kyori.adventure.text.Component

interface PacketSender {
    fun sendPacketToProxy(packet: Packet)

    fun sendPacketAll(packet: Packet)

    fun sendPacket(port: Int, packet: Packet)

    fun sendPacket(name: String, packet: Packet)

    @Deprecated("1.0.2 에서 삭제 될 예정")
    fun broadcast(message: String, logging: Boolean)

    fun broadcast(message: Component, logging: Boolean)

    @Deprecated("1.0.2 에서 삭제 될 예정")
    fun sendMessageToChannel(channel: NettyChannel, message: String, logging: Boolean)

    fun sendMessageToChannel(channel: NettyChannel, message: Component, logging: Boolean)

    @Deprecated("1.0.2 에서 삭제 될 예정")
    fun sendMessageToPlayers(players: List<NettyPlayer>, message: String, logging: Boolean)

    @Deprecated("1.0.2 에서 삭제 될 예정")
    fun sendMessageToPlayer(player: NettyPlayer, message: String, logging: Boolean)

    fun sendMessageToPlayers(players: List<NettyPlayer>, message: Component, logging: Boolean)

    fun sendMessageToPlayer(player: NettyPlayer, message: Component, logging: Boolean)
}