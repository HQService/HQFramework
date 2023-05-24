package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.AbstractPacket

class HandShakePacket : AbstractPacket {
    var port: Int = 0
        private set

    constructor()
    constructor(port: Int) {
        this.port = port
    }

    override fun write(buf: ByteBuf) {
        buf.writeInt(port)
    }

    override fun read(buf: ByteBuf) {
        port = buf.readInt()
    }
}