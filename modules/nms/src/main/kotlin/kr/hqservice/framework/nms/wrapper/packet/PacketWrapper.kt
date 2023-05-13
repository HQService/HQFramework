package kr.hqservice.framework.nms.wrapper.packet

import kr.hqservice.framework.core.component.HQSimpleComponent
import kr.hqservice.framework.nms.wrapper.NmsWrapper

abstract class PacketWrapper(
    private val instance: Any
) : NmsWrapper, HQSimpleComponent {

    fun getPacketInstance(): Any {
        return instance
    }

}