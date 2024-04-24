package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet

class ShutdownPacket(var shutdownTarget: Boolean) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeBoolean(shutdownTarget)
    }

    override fun read(buf: ByteBuf) {
        shutdownTarget = buf.readBoolean()
    }
}