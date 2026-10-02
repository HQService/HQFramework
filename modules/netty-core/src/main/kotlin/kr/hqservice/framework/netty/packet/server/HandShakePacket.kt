package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

class HandShakePacket(var port: Int, var secret: String = "") : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeInt(port)
        buf.writeString(secret)
    }

    override fun read(buf: ByteBuf) {
        port = buf.readInt()
        secret = buf.readString()
    }
}