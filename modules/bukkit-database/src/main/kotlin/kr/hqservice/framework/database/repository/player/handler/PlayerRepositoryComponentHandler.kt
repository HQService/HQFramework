package kr.hqservice.framework.database.repository.player.handler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.bukkit.Server
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.logging.Level
import java.util.logging.Logger

@ComponentHandler
class PlayerRepositoryComponentHandler(
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    private val server: Server,
    private val logger: Logger
) : HQComponentHandler<PlayerRepository<*>> {
    override fun setup(element: PlayerRepository<*>) {
        playerRepositoryRegistry.register(element)
    }

    override fun teardown(element: PlayerRepository<*>) {
        runBlocking {
            runCatching {
                newSuspendedTransaction(Dispatchers.IO) {
                    server.onlinePlayers.forEach { player -> saveIfLoaded(element, player) }
                }
            }.onFailure { logger.log(Level.SEVERE, "failed to save online players for ${element::class.simpleName}", it) }
        }
        playerRepositoryRegistry.unregister(element)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun saveIfLoaded(repository: PlayerRepository<*>, player: Player) {
        val typed = repository as PlayerRepository<Any>
        val value = typed[player.uniqueId] ?: return
        typed.save(player, value)
        typed.remove(player.uniqueId)
    }
}
