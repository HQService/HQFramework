package kr.hqservice.framework.nms.virtual.registry.impl

import io.netty.util.internal.ConcurrentSet
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
class VirtualHandlerRegistryImpl : VirtualHandlerRegistry {
    private val handlers = ConcurrentHashMap<UUID, ConcurrentSet<VirtualHandler>>()
    private var loadHandler: (suspend (UUID) -> Unit)? = null

    fun setLoadHandler(handler: suspend (UUID) -> Unit) {
        this.loadHandler = handler
    }

    override fun register(uniqueId: UUID, handler: VirtualHandler) {
        handlers.computeIfAbsent(uniqueId) { ConcurrentSet() }
            .add(handler)
    }

    override fun unregister(uniqueId: UUID, handler: VirtualHandler) {
        handlers[uniqueId]?.remove(handler)
    }

    override fun cleanup(uniqueId: UUID) {
        handlers.remove(uniqueId)
    }

    override fun getHandlers(uniqueId: UUID): Set<VirtualHandler> {
        return handlers[uniqueId] ?: emptySet()
    }

    fun findLoadHandler(): (suspend (UUID) -> Unit)? = loadHandler
}