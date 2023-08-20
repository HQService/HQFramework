package kr.hqservice.framework.database.repository.player.handler

import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
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