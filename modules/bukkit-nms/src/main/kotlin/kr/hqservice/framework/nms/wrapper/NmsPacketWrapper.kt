package kr.hqservice.framework.nms.wrapper

import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kotlin.reflect.KClass

abstract class NmsPacketWrapper : NmsWrapper, HQSimpleComponent {
    abstract fun getClass(): KClass<*>

    abstract fun createPacket(): Any

    final override fun getUnwrappedInstance(): Any {
        return createPacket()
    }
}