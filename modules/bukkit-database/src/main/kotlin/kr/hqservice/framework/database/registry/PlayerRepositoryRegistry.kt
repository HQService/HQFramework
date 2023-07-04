package kr.hqservice.framework.database.registry

import kr.hqservice.framework.database.component.repository.HQPlayerRepository
import kr.hqservice.framework.global.core.component.HQSimpleComponent

interface PlayerRepositoryRegistry : HQSimpleComponent {
    fun register(repository: HQPlayerRepository<*>)

    fun unregister(repository: HQPlayerRepository<*>)

    fun getAll(): Collection<HQPlayerRepository<*>>
}