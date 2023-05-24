package kr.hqservice.framework.nms.wrapper.packet

import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kotlin.reflect.KClass

abstract class PacketWrapper : NmsWrapper, HQSimpleComponent {

    abstract fun getClass(): KClass<*>

    abstract fun createPacket(): Any

}