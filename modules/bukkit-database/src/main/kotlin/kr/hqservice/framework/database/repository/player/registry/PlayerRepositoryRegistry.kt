package kr.hqservice.framework.database.repository.player.registry

import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.global.core.component.HQSimpleComponent

interface PlayerRepositoryRegistry : HQSimpleComponent {
    fun register(repository: PlayerRepository<*>)

    fun unregister(repository: PlayerRepository<*>)

    fun getAll(): Collection<PlayerRepository<*>>
}