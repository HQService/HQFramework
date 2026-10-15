package kr.hqservice.framework.database.repository.player.handler

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.lifecycle.PlayerDataLifecycle
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ComponentHandler
class PlayerRepositoryComponentHandler(
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    private val lifecycle: PlayerDataLifecycle,
    private val logger: Logger
) : HQComponentHandler<PlayerRepository<*>> {
    override fun setup(element: PlayerRepository<*>) {
        playerRepositoryRegistry.register(element)
        lifecycle.attach(element)
    }

    override fun teardown(element: PlayerRepository<*>) {
        runBlocking(owningPluginContext(element)) {
            withTimeoutOrNull(5_000) {
                lifecycle.flushRepositoryForTeardown(element)
            } ?: logger.warning("saving online players for ${element::class.simpleName} timed out")
        }
        playerRepositoryRegistry.unregister(element)
    }

    private fun owningPluginContext(element: PlayerRepository<*>): CoroutineContext {
        return runCatching { PluginCoroutineContextElement(JavaPlugin.getProvidingPlugin(element::class.java)) }
            .getOrDefault(EmptyCoroutineContext)
    }
}
