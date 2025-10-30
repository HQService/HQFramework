package kr.hqservice.framework.netty.packet

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class PacketWrapper<T : Packet>(
    var clazz: KClass<T>,
    val codecClass: Class<*>,
    val primaryConstructor: KFunction<T>
)