package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writeString

class RelayingPacket : AbstractPacket {
    private lateinit var packet: AbstractPacket
    private var targetServer: String? = null
    private var relay: ByteArray? = null

    constructor()

    constructor(packet: AbstractPacket) {
        this.targetServer = null
        this.packet = packet
    }

    constructor(targetServer: String, packet: AbstractPacket) {
        this.targetServer = targetServer
        this.packet = packet
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(targetServer?: "-1")
        buf.writeString(packet::class.qualifiedName?: throw IllegalStateException("fuck"))
        buf.writeBoolean(packet.isCallbackResult())
        packet.write(buf)
    }

    override fun read(buf: ByteBuf) {
        targetServer = buf.readString()
        val relayAble = ByteArray(buf.readableBytes())
        buf.readBytes(relayAble)
        relay = relayAble
    }

    fun getTargetServer(): String {
        return targetServer!!
    }

    fun getRelay(): ByteArray {
        return relay!!
    }
}