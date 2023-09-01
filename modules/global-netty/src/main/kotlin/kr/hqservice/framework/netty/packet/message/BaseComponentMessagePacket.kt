package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readPlayers
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writePlayers
import kr.hqservice.framework.netty.packet.extension.writeString
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

class BaseComponentMessagePacket(
    var message: BaseComponent,
    var logging: Boolean,
    var receivers: List<NettyPlayer>
) : Packet() {

    override fun write(buf: ByteBuf) {
        buf.writeString(ComponentSerializer.toString(message))
        buf.writeBoolean(logging)
        buf.writePlayers(receivers)
    }

    override fun read(buf: ByteBuf) {
        message = ComponentSerializer.parse(buf.readString()).first()
        logging = buf.readBoolean()
        receivers = buf.readPlayers()
    }
}