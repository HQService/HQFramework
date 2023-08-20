package kr.hqservice.framework.database.repository.handler

import kr.hqservice.framework.database.repository.PlayerRepository
import kr.hqservice.framework.database.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler

@ComponentHandler
class PlayerRepositoryComponentHandler(
    private val playerRepositoryRegistry: PlayerRepositoryRegistry
) : HQComponentHandler<PlayerRepository<*>> {
    override fun setup(element: PlayerRepository<*>) {
        playerRepositoryRegistry.register(element)
    }

    override fun teardown(element: PlayerRepository<*>) {
        playerRepositoryRegistry.unregister(element)
    }
}