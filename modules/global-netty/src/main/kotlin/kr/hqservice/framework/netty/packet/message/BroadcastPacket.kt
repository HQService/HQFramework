package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readChannel
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeChannel
import kr.hqservice.framework.netty.packet.extension.writeString

class BroadcastPacket(
    var message: String,
    var logging: Boolean,
    var targetChannel: NettyChannel?
) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeString(message)
        buf.writeBoolean(logging)
        buf.writeChannel(targetChannel)
    }

    override fun read(buf: ByteBuf) {
        message = buf.readString()
        logging = buf.readBoolean()
        targetChannel = buf.readChannel()
    }
}