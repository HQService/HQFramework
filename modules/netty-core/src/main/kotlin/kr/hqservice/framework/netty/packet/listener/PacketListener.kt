package kr.hqservice.framework.netty.packet.listener

import kr.hqservice.framework.global.core.component.Scannable
import kr.hqservice.framework.netty.packet.Packet
import kotlin.reflect.KClass

@Scannable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PacketListener(val outbound: Array<KClass<out Packet>> = [])
