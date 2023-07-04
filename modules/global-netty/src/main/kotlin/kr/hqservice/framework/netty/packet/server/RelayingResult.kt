package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet
import java.lang.UnsupportedOperationException

class RelayingResult(
    val array: ByteArray
) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeBytes(array)
    }

    override fun read(buf: ByteBuf) {
        throw UnsupportedOperationException("relaying result shouldn't access read method")
    }
}