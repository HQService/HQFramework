package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

private const val MAX_RELAY_BYTES = 16 * 1024 * 1024

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
        packet = null
        targetServer = buf.readString()
        val remaining = buf.readableBytes()
        if (remaining > MAX_RELAY_BYTES) {
            throw IllegalArgumentException("relay payload too large: $remaining bytes")
        }
        val relayAble = ByteArray(remaining)
        buf.readBytes(relayAble)
        relay = relayAble
    }

    fun getRelayByte(): ByteArray {
        return relay ?: ByteArray(0)
    }
}
