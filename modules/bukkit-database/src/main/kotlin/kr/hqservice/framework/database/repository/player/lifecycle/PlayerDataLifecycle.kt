package kr.hqservice.framework.database.repository.player.lifecycle

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.repository.player.FlushRequester
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.database.repository.player.session.AcquireResult
import kr.hqservice.framework.database.repository.player.session.OwnershipTimeoutException
import kr.hqservice.framework.database.repository.player.session.SessionCoordinator
import kr.hqservice.framework.netty.api.PacketSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.PluginManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.EmptyCoroutineContext

@Listener
class PlayerDataLifecycle(
    private val plugin: HQBukkitPlugin,
    private val repositories: PlayerRepositoryRegistry,
    private val sessions: PlayerSessionRegistry,
    private val coordinator: SessionCoordinator,
    private val settings: PlayerDataSettings,
    private val loading: LoadingPlayers,
    private val pluginManager: PluginManager,
    private val packetSender: PacketSender,
    private val nettyService: HQNettyService,
    private val logger: Logger,
) {
    private val playerScopes = PlayerScopes(plugin, Dispatchers.IO)
    private val hints = ConcurrentHashMap<UUID, CompletableDeferred<Unit>>()
    private val scheduler = FlushScheduler(sessions, { repositories.getAll() }, coordinator, settings, playerScopes, logger, ::onOwnershipLost)
    private val schedulerJob = scheduler.start(plugin)

    init {
        repositories.getAll().forEach(::attach)
    }

    fun attach(repository: PlayerRepository<*>) {
        repository.flushScope = plugin
        repository.flushRequester = FlushRequester { uuid, target ->
            playerScopes.launch(uuid) { scheduler.flushPlayer(uuid, listOf(target), FlushReason.EXPLICIT) }.join()
        }
    }

    fun stop() {
        schedulerJob.cancel()
    }

    suspend fun flushRepositoryForTeardown(repository: PlayerRepository<*>) {
        val owner = currentCoroutineContext()[PluginCoroutineContextElement] ?: EmptyCoroutineContext
        sessions.all().forEach { session ->
            playerScopes.launch(session.uuid, owner) {
                scheduler.flushPlayer(session.uuid, listOf(repository), FlushReason.TEARDOWN)
            }.join()
        }
    }

    fun shutdown() {
        stop()
        runBlocking {
            withTimeoutOrNull(5_000) {
                sessions.all().forEach { session ->
                    try {
                        coordinator.release(session.uuid)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.log(Level.WARNING, "failed to release player data ownership of ${session.uuid} on shutdown", e)
                    }
                    sessions.remove(session.uuid)
                }
            } ?: logger.warning("releasing player data ownership on shutdown timed out")
        }
    }

    @Subscribe
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val token = loading.begin(uuid)
        sessions.get(uuid)?.player = player
        plugin.launch(Dispatchers.Default) {
            var retained: PlayerSession? = null
            try {
                playerScopes.awaitIdle(uuid)
                val version = acquireWithRetry(uuid)
                retained = sessions.get(uuid)
                if (!isStillJoining(player)) {
                    if (retained == null) coordinator.release(uuid)
                    return@launch
                }
                loadAll(player, onlyMissing = retained != null)
                if (!register(retained ?: PlayerSession(uuid, player, version), keepOnFailure = retained != null)) {
                    if (retained == null) {
                        repositories.getAll().forEach { it.remove(uuid) }
                        coordinator.release(uuid)
                    }
                    return@launch
                }
                withContext(Dispatchers.BukkitMain) { pluginManager.callEvent(PlayerRepositoryLoadedEvent(player)) }
            } catch (e: OwnershipTimeoutException) {
                logger.log(if (e.cause != null) Level.SEVERE else Level.WARNING, "could not acquire player data ownership of $uuid within ${settings.joinTimeout.toMillis()}ms", e.cause)
                kick(player, "데이터를 아직 다른 서버에서 저장 중입니다. 잠시 후 다시 접속해 주세요")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "failed to load player data of $uuid", e)
                if (retained == null) {
                    sessions.remove(uuid)
                    repositories.getAll().forEach { it.remove(uuid) }
                    runCatching { coordinator.release(uuid) }
                }
                kick(player, "데이터를 불러오지 못했습니다")
            } finally {
                loading.end(uuid, token)
            }
        }
    }

    @Subscribe
    fun onQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        loading.remove(uuid)
        playerScopes.launch(uuid, TeardownOptionCoroutineContextElement(false)) {
            val session = sessions.get(uuid) ?: return@launch
            val targets = repositories.getAll()
            try {
                val saved = scheduler.flushPlayer(uuid, targets, FlushReason.QUIT) || targets.none { it.contains(uuid) }
                if (!saved) {
                    logger.severe("failed to save player data of $uuid on quit; ownership is kept until a retry succeeds")
                    return@launch
                }
                sessions.remove(uuid)
                coordinator.release(uuid)
                targets.forEach { it.remove(uuid) }
                if (nettyService.isEnable()) packetSender.sendPacketAll(PlayerDataSavedPacket(uuid))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "failed to release player data ownership of $uuid; release is deferred", e)
                sessions.put(session)
            }
        }
    }

    @Subscribe
    fun onPacketReceive(event: AsyncNettyPacketReceivedEvent) {
        (event.packet as? PlayerDataSavedPacket)?.let { hints[it.id]?.complete(Unit) }
    }

    private fun isStillJoining(player: Player): Boolean = player.uniqueId in loading && player.isOnline

    private suspend fun register(session: PlayerSession, keepOnFailure: Boolean): Boolean {
        var registered = false
        playerScopes.launch(session.uuid) {
            sessions.put(session)
            registered = isStillJoining(session.player)
            if (!registered && !keepOnFailure) sessions.remove(session.uuid)
        }.join()
        return registered
    }

    private suspend fun acquireWithRetry(uuid: UUID): Long {
        var lastError: Exception? = null
        var hint: CompletableDeferred<Unit>? = null
        try {
            return withTimeoutOrNull(settings.joinTimeout.toMillis()) {
                var version: Long? = null
                while (version == null) {
                    version = try {
                        (coordinator.acquire(uuid) as? AcquireResult.Acquired)?.version
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        lastError = e
                        null
                    }
                    if (version == null) hint = awaitRetry(uuid)
                }
                version
            } ?: throw OwnershipTimeoutException(uuid, lastError)
        } finally {
            hint?.let { hints.remove(uuid, it) }
        }
    }

    private suspend fun awaitRetry(uuid: UUID): CompletableDeferred<Unit> {
        val hint = hints.computeIfAbsent(uuid) { CompletableDeferred() }
        withTimeoutOrNull(settings.retryInterval.toMillis()) { hint.await() }
        if (hint.isCompleted) hints.remove(uuid, hint)
        return hint
    }

    private suspend fun loadAll(player: Player, onlyMissing: Boolean) {
        try {
            loadOnce(player, onlyMissing)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(Level.WARNING, "failed to load player data of ${player.uniqueId}, retrying once", e)
            delay(250)
            loadOnce(player, onlyMissing)
        }
    }

    private suspend fun loadOnce(player: Player, onlyMissing: Boolean) {
        newSuspendedTransaction(Dispatchers.IO) {
            repositories.getAll()
                .filterNot { onlyMissing && it.contains(player.uniqueId) }
                .forEach { load(it, player) }
        }
    }

    private suspend fun <V : Any> load(repository: PlayerRepository<V>, player: Player) {
        repository.put(player.uniqueId, repository.load(player))
    }

    private suspend fun onOwnershipLost(session: PlayerSession) {
        kick(session.player, "다른 서버가 데이터를 가져갔습니다")
    }

    private suspend fun kick(player: Player, message: String) {
        withContext(Dispatchers.BukkitMain) {
            if (player.isOnline) player.kickPlayer(message)
        }
    }
}
