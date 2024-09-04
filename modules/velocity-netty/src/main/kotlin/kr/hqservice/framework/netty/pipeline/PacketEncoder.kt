package kr.hqservice.framework.netty.pipeline

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.writeString
import kr.hqservice.framework.netty.packet.server.RelayingResult

class PacketEncoder : MessageToByteEncoder<Packet>() {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, packet: Packet?, out: ByteBuf) {
        if (packet == null)
            throw IllegalArgumentException("packet is null")

        if (packet is RelayingResult) {
            packet.write(out)
        } else {
            val packetClass = packet::class
            if (Direction.OUTBOUND.findPacketByClass(packetClass) == null)
                return

            out.writeString(
                packetClass.qualifiedName ?: throw IllegalStateException("'${packetClass}' has not qualified name")
            )

            out.writeBoolean(packet.isCallbackResult())
            packet.write(out)
        }
    }
}