package kr.hqservice.framework.netty.packet

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class PacketWrapper<T : AbstractPacket>(
    var clazz: KClass<T>,
    var constructor: KFunction<T>
)