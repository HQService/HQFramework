package kr.hqservice.framework.database.packet

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readPlayer
import kr.hqservice.framework.netty.packet.extension.writePlayer

class PlayerDataSavedPacket(
    var player: NettyPlayer
) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writePlayer(player)
    }

    override fun read(buf: ByteBuf) {
        player = buf.readPlayer()
    }
}