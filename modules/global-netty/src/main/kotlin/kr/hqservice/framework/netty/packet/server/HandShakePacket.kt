package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet

class HandShakePacket(var port: Int) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeInt(port)
    }

    override fun read(buf: ByteBuf) {
        port = buf.readInt()
    }
}