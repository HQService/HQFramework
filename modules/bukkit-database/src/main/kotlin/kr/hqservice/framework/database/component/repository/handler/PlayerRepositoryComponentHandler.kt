package kr.hqservice.framework.database.component.repository.handler

import kr.hqservice.framework.database.component.repository.HQPlayerRepository
import kr.hqservice.framework.database.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler

@ComponentHandler
class PlayerRepositoryComponentHandler(private val playerRepositoryRegistry: PlayerRepositoryRegistry) :
    HQComponentHandler<HQPlayerRepository<*>> {
    override fun setup(element: HQPlayerRepository<*>) {
        playerRepositoryRegistry.register(element)
    }

    override fun teardown(element: HQPlayerRepository<*>) {
        playerRepositoryRegistry.unregister(element)
    }
}