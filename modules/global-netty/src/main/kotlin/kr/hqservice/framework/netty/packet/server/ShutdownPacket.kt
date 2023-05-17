package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.AbstractPacket

class ShutdownPacket : AbstractPacket() {
    var shutdownTarget: Boolean = false
        private set

    override fun write(buf: ByteBuf) {
        buf.writeBoolean(shutdownTarget)
    }

    override fun read(buf: ByteBuf) {
        shutdownTarget = buf.readBoolean()
    }
}