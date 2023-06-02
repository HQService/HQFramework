package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

class RelayingPacket(
    var packet: Packet,
    var targetServer: String = "-1"
) : Packet() {
    private var relay: ByteArray? = null

    override fun write(buf: ByteBuf) {
        buf.writeString(targetServer)
        buf.writeString(packet::class.qualifiedName!!)
        buf.writeBoolean(packet.isCallbackResult())
        packet.write(buf)
    }

    override fun read(buf: ByteBuf) {
        targetServer = buf.readString()
        val relayAble = ByteArray(buf.readableBytes())
        buf.readBytes(relayAble)
        relay = relayAble
    }

    fun getRelay(): ByteArray {
        return relay!!
    }
}