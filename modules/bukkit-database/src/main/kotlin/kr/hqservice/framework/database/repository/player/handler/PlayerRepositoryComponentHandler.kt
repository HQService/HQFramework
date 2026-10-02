package kr.hqservice.framework.database.repository.player.handler

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
        runBlocking(owningPluginContext(element)) {
            withTimeoutOrNull(5_000) {
                server.onlinePlayers.forEach { player -> saveIfLoaded(element, player) }
            } ?: logger.warning("saving online players for ${element::class.simpleName} timed out")
        }
        playerRepositoryRegistry.unregister(element)
    }

    private fun owningPluginContext(element: PlayerRepository<*>): CoroutineContext {
        return runCatching { PluginCoroutineContextElement(JavaPlugin.getProvidingPlugin(element::class.java)) }
            .getOrDefault(EmptyCoroutineContext)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun saveIfLoaded(element: PlayerRepository<*>, player: Player) {
        val repository = element as PlayerRepository<Any>
        val value = repository[player.uniqueId] ?: return
        runCatching { newSuspendedTransaction { repository.save(player, value) } }
            .onSuccess { repository.remove(player.uniqueId, value) }
            .onFailure {
                if (it is CancellationException) throw it
                logger.log(Level.SEVERE, "failed to save ${player.uniqueId} for ${element::class.simpleName}", it)
            }
    }
}
