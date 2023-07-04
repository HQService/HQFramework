package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

class RelayingPacket(
    var packet: Packet?,
    var targetServer: String = "-1",
    private var relay: ByteArray? = null
) : Packet() {

    override fun write(buf: ByteBuf) {
        buf.writeString(targetServer)
        packet?.apply {
            buf.writeString(this::class.qualifiedName!!)
            buf.writeBoolean(this.isCallbackResult())
            write(buf)
        }
        relay?.apply(buf::writeBytes)
    }

    override fun read(buf: ByteBuf) {
        targetServer = buf.readString()
        val relayAble = ByteArray(buf.readableBytes())
        buf.readBytes(relayAble)
        relay = relayAble
    }

    fun getRelayByte(): ByteArray {
        return relay!!
    }
}