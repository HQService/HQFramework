package kr.hqservice.framework.netty.packet.player

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.*

class PlayerConnectionPacket(
    var player: NettyPlayer,
    var state: PlayerConnectionState,
    var sourceChannel: NettyChannel?
) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writePlayer(player)
        buf.writeString(state.name)
        buf.writeChannel(sourceChannel)
    }

    override fun read(buf: ByteBuf) {
        player = buf.readPlayer()
        state = PlayerConnectionState.valueOf(buf.readString())
        sourceChannel = buf.readChannel()
    }
}