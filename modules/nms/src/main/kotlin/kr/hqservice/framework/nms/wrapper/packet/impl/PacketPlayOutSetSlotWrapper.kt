package kr.hqservice.framework.nms.wrapper.packet.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.packet.PacketWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

class PacketPlayOutSetSlotWrapper(
    private val containerId: Int,
    private val stateId: Int,
    private val slot: Int,
    private val nmsItemStack: Any
) : PacketWrapper(), KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()
    private val packetClass = reflectionUtil.getNmsClass("PacketPlayOutSetSlot", Version.V_15.handle("network.protocol.game"))

    override fun getClass(): KClass<*> {
        return packetClass
    }

    override fun createPacket(): Any {
        return packetClass.constructors.first { it.parameters.size == 4 }
            .call(containerId, stateId, slot, nmsItemStack)
    }

}