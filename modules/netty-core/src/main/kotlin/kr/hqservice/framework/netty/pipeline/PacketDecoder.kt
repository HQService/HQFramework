package kr.hqservice.framework.netty.pipeline

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.server.HandShakePacket

private val HANDSHAKE_PACKET_NAME = HandShakePacket::class.qualifiedName!!

class PacketDecoder(
    private val connectionState: () -> ConnectionState = { ConnectionState.CONNECTED }
) : MessageToMessageDecoder<ByteBuf>() {
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val packetName = buf.readString()
        if (connectionState() != ConnectionState.CONNECTED && packetName != HANDSHAKE_PACKET_NAME) {
            buf.skipBytes(buf.readableBytes())
            return
        }
        val callbackResult = buf.readBoolean()

        val wrapper = Direction.INBOUND.findPacketByName(packetName) ?: run {
            buf.skipBytes(buf.readableBytes())
            return
        }
        out.add(wrapper.decode(buf, callbackResult))
    }
}