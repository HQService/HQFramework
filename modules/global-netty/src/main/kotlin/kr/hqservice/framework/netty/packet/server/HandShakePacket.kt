package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

class HandShakePacket : AbstractPacket {
    private var port: Int = 0

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