package kr.hqservice.framework.database.registry.impl

import kr.hqservice.framework.database.component.repository.HQPlayerRepository
import kr.hqservice.framework.database.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton

@Component
@HQSingleton(binds = [PlayerRepositoryRegistry::class])
class PlayerRepositoryRegistryImpl : PlayerRepositoryRegistry {
    private val repositories: MutableList<HQPlayerRepository<*>> = mutableListOf()

    override fun register(repository: HQPlayerRepository<*>) {
        repositories.add(repository)
    }

    override fun unregister(repository: HQPlayerRepository<*>) {
        repositories.remove(repository)
    }

    override fun getAll(): Collection<HQPlayerRepository<*>> {
        return repositories
    }
}