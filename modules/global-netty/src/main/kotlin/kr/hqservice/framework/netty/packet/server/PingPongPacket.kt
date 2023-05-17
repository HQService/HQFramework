package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.AbstractPacket

class PingPongPacket : AbstractPacket {
    var receivedTime: Long = 0
        private set
    var time: Long = 0
        private set

    constructor()
    constructor(receivedTime: Long, time: Long) {
        this.receivedTime = receivedTime
        this.time = time
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(receivedTime)
        buf.writeLong(time)
    }

    override fun read(buf: ByteBuf) {
        receivedTime = buf.readLong()
        time = buf.readLong()
    }
}