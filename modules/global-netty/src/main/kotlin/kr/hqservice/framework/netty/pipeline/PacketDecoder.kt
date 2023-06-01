package kr.hqservice.framework.netty.pipeline

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.extension.readString
import kotlin.jvm.Throws
import kotlin.reflect.KClass

class PacketDecoder : MessageToMessageDecoder<ByteBuf>() {

    @Suppress("unchecked_cast")
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val packetName = buf.readString()
        val callbackResult = buf.readBoolean()

        val packetClass = Class.forName(packetName).kotlin as KClass<AbstractPacket>
        val wrapper = Direction.INBOUND.findPacketByClass(packetClass)?: throw NullPointerException("unregistered packet ('${packetName}')")
        val packet = wrapper.constructor.call()
        packet.setCallbackResult(callbackResult)

        packet.read(buf)
        out.add(packet)
    }
}