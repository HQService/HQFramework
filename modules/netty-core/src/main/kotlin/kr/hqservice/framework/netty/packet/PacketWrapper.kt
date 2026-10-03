package kr.hqservice.framework.netty.packet

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor

class PacketWrapper<T : Packet>(
    var clazz: KClass<T>,
    val codecClass: Class<*>,
    val primaryConstructor: KFunction<T>
) {
    private val codecConstructor: Constructor<*> = codecClass.getConstructor()
    private val constructor: Constructor<T> = primaryConstructor.javaConstructor
        ?: throw IllegalArgumentException("packet ${clazz.qualifiedName} has no java constructor")
    private val fields: Array<Field> = primaryConstructor.parameters.map { parameter ->
        codecClass.getDeclaredField(parameter.name ?: throw IllegalArgumentException("packet ${clazz.qualifiedName} has an unnamed constructor parameter"))
            .apply { isAccessible = true }
    }.toTypedArray()

    init {
        constructor.isAccessible = true
    }

    fun decode(buf: io.netty.buffer.ByteBuf, callbackResult: Boolean): T {
        val codec = codecConstructor.newInstance() as Packet
        codec.read(buf)
        val arguments = arrayOfNulls<Any>(fields.size)
        for (index in fields.indices) arguments[index] = fields[index].get(codec)
        val packet = constructor.newInstance(*arguments)
        packet.setCallbackResult(callbackResult)
        return packet
    }
}
