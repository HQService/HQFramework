package kr.hqservice.framework.database.repository.player.registry.impl

import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Singleton
import java.util.concurrent.CopyOnWriteArrayList

@Component
@Singleton(binds = [PlayerRepositoryRegistry::class])
class PlayerRepositoryRegistryImpl : PlayerRepositoryRegistry {
    private val repositories: MutableList<PlayerRepository<*>> = CopyOnWriteArrayList()

    override fun register(repository: PlayerRepository<*>) {
        repositories.add(repository)
    }

    override fun unregister(repository: PlayerRepository<*>) {
        repositories.remove(repository)
    }

    override fun getAll(): Collection<PlayerRepository<*>> {
        return repositories
    }
}