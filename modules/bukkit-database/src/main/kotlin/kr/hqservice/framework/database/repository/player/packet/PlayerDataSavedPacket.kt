package kr.hqservice.framework.database.repository.player.packet

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readUUID
import kr.hqservice.framework.netty.packet.extension.writeUUID
import java.util.UUID

class PlayerDataSavedPacket(
    //var player: NettyPlayer
    var id: UUID
) : Packet() {
    override fun write(buf: ByteBuf) {
        //buf.writePlayer(player)
        buf.writeUUID(id)
    }

    override fun read(buf: ByteBuf) {
        //player = buf.readPlayer()
        id = buf.readUUID()
    }
}