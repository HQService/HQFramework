package kr.hqservice.framework.nms.virtual.registry.impl

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
class VirtualHandlerRegistryImpl : VirtualHandlerRegistry {
    private val handlers = ConcurrentHashMap<UUID, MutableSet<VirtualHandler>>()

    override fun register(uniqueId: UUID, handler: VirtualHandler) {
        handlers.computeIfAbsent(uniqueId) { ConcurrentHashMap.newKeySet() }
            .add(handler)
    }

    override fun unregister(uniqueId: UUID, handler: VirtualHandler) {
        handlers[uniqueId]?.remove(handler)
    }

    override fun cleanup(uniqueId: UUID) {
        handlers.remove(uniqueId)?.forEach { handler -> runCatching { handler.close() } }
    }

    override fun getHandlers(uniqueId: UUID): Set<VirtualHandler> {
        return handlers[uniqueId] ?: emptySet()
    }
}