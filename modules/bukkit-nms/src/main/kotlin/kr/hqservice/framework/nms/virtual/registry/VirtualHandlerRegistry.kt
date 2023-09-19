package kr.hqservice.framework.nms.virtual.registry

import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import java.util.*

interface VirtualHandlerRegistry {
    fun register(uniqueId: UUID, handler: VirtualHandler)

    fun unregister(uniqueId: UUID, handler: VirtualHandler)

    fun cleanup(uniqueId: UUID)

    fun getHandlers(uniqueId: UUID): Set<VirtualHandler>
}