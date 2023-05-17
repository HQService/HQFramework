package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

class HandShakePacket : AbstractPacket {
    private var host: String = ""
    private var port: Int = 0

    constructor()
    constructor(host: String, port: Int) {
        this.host = host
        this.port = port
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(host)
        buf.writeInt(port)
    }

    override fun read(buf: ByteBuf) {
        host = buf.readString()
        port = buf.readInt()
    }
}