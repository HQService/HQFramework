package kr.hqservice.framework.netty.pipeline

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy
import net.bytebuddy.implementation.MethodCall
import java.util.*
import kotlin.jvm.Throws
import kotlin.reflect.KClass

class PacketDecoder : MessageToMessageDecoder<ByteBuf>() {
    @Suppress("unchecked_cast")
    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val packetName = buf.readString()
        val callbackResult = buf.readBoolean()

        val packetClass = Class.forName(packetName).kotlin as KClass<Packet>
        val wrapper = Direction.INBOUND.findPacketByClass(packetClass)?: throw NullPointerException("unregistered packet ('${packetName}')")
        val codecClass = wrapper.codecClass
        val codecPacket = codecClass.getConstructor().newInstance()

        codecClass.getMethod("read", ByteBuf::class.java).invoke(codecPacket, buf)

        val packetConstructor = wrapper.primaryConstructor
        val params = mutableListOf<Any?>()

        packetConstructor.parameters.forEach {
            val field = codecClass.getDeclaredField(it.name?: throw IllegalArgumentException("not found field ${it.name}"))
            field.isAccessible = true
            params.add(field.get(codecPacket))
            field.isAccessible = false
        }

        val packet = packetConstructor.call(*params.toTypedArray())
        packetClass.java.getMethod("setCallbackResult", Boolean::class.java).invoke(packet, callbackResult)
        out.add(packet)
    }
}