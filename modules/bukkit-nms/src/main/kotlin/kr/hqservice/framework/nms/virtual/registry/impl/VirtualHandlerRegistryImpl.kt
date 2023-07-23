package kr.hqservice.framework.nms.virtual.registry.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import java.util.*

@Component
@Singleton(binds = [VirtualHandlerRegistry::class])
class VirtualHandlerRegistryImpl : VirtualHandlerRegistry {
    private val handlers = mutableMapOf<UUID, MutableList<VirtualHandler>>()
    override fun register(uniqueId: UUID, handler: VirtualHandler) {
        handlers.computeIfAbsent(uniqueId) { mutableListOf() }
            .add(handler)
    }

    override fun unregister(uniqueId: UUID, handler: VirtualHandler) {
        handlers[uniqueId]?.remove(handler)
    }

    override fun cleanup(uniqueId: UUID) {
        handlers.remove(uniqueId)
    }

    override fun getHandlers(uniqueId: UUID): List<VirtualHandler> {
        return handlers[uniqueId] ?: emptyList()
    }
}