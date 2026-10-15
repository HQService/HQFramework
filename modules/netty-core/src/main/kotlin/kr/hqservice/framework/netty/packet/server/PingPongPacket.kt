package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet

class PingPongPacket(var receivedTime: Long, var time: Long) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeLong(receivedTime)
        buf.writeLong(time)
    }

    override fun read(buf: ByteBuf) {
        receivedTime = buf.readLong()
        time = buf.readLong()
    }
}