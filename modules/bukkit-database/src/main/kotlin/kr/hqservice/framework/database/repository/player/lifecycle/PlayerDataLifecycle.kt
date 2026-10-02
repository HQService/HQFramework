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
import kotlinx.serialization.json.Json
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.redis.RedisProvider
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.CachedPlayerRepository
import kr.hqservice.framework.database.repository.player.FlushRequester
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.cache.LettucePlayerDataCache
import kr.hqservice.framework.database.repository.player.cache.PlayerDataCache
import kr.hqservice.framework.database.repository.player.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.database.repository.player.session.AcquireResult
import kr.hqservice.framework.database.repository.player.session.OwnershipTimeoutException
import kr.hqservice.framework.database.repository.player.session.RedisSessionCoordinator
import kr.hqservice.framework.database.repository.player.session.SessionCoordinator
import kr.hqservice.framework.netty.api.PacketSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.PluginManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.EmptyCoroutineContext

@Listener
class PlayerDataLifecycle(
    private val plugin: HQBukkitPlugin,
    private val repositories: PlayerRepositoryRegistry,
    private val sessions: PlayerSessionRegistry,
    private val coordinator: SessionCoordinator,
    private val database: Database,
    private val settings: PlayerDataSettings,
    private val loading: LoadingPlayers,
    private val pluginManager: PluginManager,
    private val packetSender: PacketSender,
    private val nettyService: HQNettyService,
    private val logger: Logger,
    private val redisSettings: RedisSettings,
    private val redisProvider: RedisProvider,
    private val json: Json,
) {
    private val playerScopes = PlayerScopes(plugin, Dispatchers.IO)
    private val hints = ConcurrentHashMap<UUID, CompletableDeferred<Unit>>()
    internal var cacheFactory: () -> PlayerDataCache = { LettucePlayerDataCache(redisProvider) }
    private val cache: PlayerDataCache by lazy { cacheFactory() }
    private val scheduler = FlushScheduler(sessions, { repositories.getAll() }, coordinator, database, settings, playerScopes, logger, ::onOwnershipLost)
    private val schedulerJob = scheduler.start(plugin)
    private val releasedSubscription = AtomicReference<AutoCloseable?>()

    init {
        repositories.getAll().forEach(::attach)
        plugin.launch(Dispatchers.IO) {
            runCatching { coordinator.onReleased { uuid -> hints[uuid]?.complete(Unit) } }
                .onSuccess { if (!releasedSubscription.compareAndSet(null, it)) it.close() }
                .onFailure { logger.log(Level.WARNING, "failed to subscribe to player data release notifications; falling back to polling", it) }
        }
    }

    fun attach(repository: PlayerRepository<*>) {
        repository.flushScope = plugin
        repository.flushRequester = FlushRequester { uuid, target ->
            var saved = false
            playerScopes.launch(uuid) { saved = scheduler.flushPlayer(uuid, listOf(target), FlushReason.EXPLICIT) }.join()
            saved
        }
        if (repository is CachedPlayerRepository<*> && redisSettings.enabled) {
            repository.cache = cache
            repository.cacheSettings = redisSettings
            repository.json = json
            repository.logger = logger
            repository.cacheWriter = { uuid, block -> playerScopes.launch(uuid) { block() } }
            repository.fence = (coordinator as? RedisSessionCoordinator)?.let { redis -> redis::fence }
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
        releasedSubscription.getAndSet(AutoCloseable { })?.close()
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
                withTimeoutOrNull(settings.joinTimeout.toMillis()) { playerScopes.awaitIdle(uuid) }
                    ?: throw OwnershipTimeoutException(uuid)
                val version = acquireWithRetry(uuid)
                retained = sessions.get(uuid)?.let { keepUnlessSuperseded(it, version) }
                if (!isStillJoining(player, token)) {
                    if (retained == null && mayCleanUp(uuid, token)) coordinator.release(uuid)
                    return@launch
                }
                withTimeoutOrNull(settings.joinTimeout.toMillis()) { loadAll(player, token, onlyMissing = retained != null) }
                    ?: throw IllegalStateException("loading player data of $uuid did not finish within ${settings.joinTimeout.toMillis()}ms")
                if (!register(retained ?: PlayerSession(uuid, player, version), token)) {
                    if (retained == null && mayCleanUp(uuid, token)) {
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
                if (retained == null && mayCleanUp(uuid, token)) {
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

    private fun mayCleanUp(uuid: UUID, token: Long): Boolean = !loading.isSuperseded(uuid, token) && sessions.get(uuid) == null

    private fun isStillJoining(player: Player, token: Long): Boolean = loading.isCurrent(player.uniqueId, token) && player.isOnline

    private suspend fun register(session: PlayerSession, token: Long): Boolean {
        var registered = false
        playerScopes.launch(session.uuid) {
            registered = isStillJoining(session.player, token)
            if (registered) sessions.put(session)
        }.join()
        return registered
    }

    private fun keepUnlessSuperseded(retained: PlayerSession, version: Long): PlayerSession? {
        if (retained.version == version) return retained
        logger.info("retained session of ${retained.uuid} superseded (version ${retained.version}, current $version); discarding its unsaved data and loading fresh")
        sessions.remove(retained.uuid)
        repositories.getAll().forEach { it.remove(retained.uuid) }
        return null
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

    private suspend fun loadAll(player: Player, token: Long, onlyMissing: Boolean) {
        try {
            loadOnce(player, token, onlyMissing)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(Level.WARNING, "failed to load player data of ${player.uniqueId}, retrying once", e)
            delay(250)
            loadOnce(player, token, onlyMissing)
        }
    }

    private suspend fun loadOnce(player: Player, token: Long, onlyMissing: Boolean) {
        val uuid = player.uniqueId
        val targets = repositories.getAll().filterNot { onlyMissing && it.contains(uuid) }
        val hot = targets.mapNotNull { readHot(it, uuid) }
        val cold = targets - hot.map { it.repository }.toSet()
        val loaded = newSuspendedTransaction(Dispatchers.IO, database) { cold.map { loadCold(it, player) } }
        if (!loading.isCurrent(uuid, token)) return
        (hot + loaded).forEach { it.publish(uuid) }
    }

    private class Loaded<V : Any>(val repository: PlayerRepository<V>, val value: V, val fromCache: Boolean) {
        suspend fun publish(uuid: UUID) {
            val cached = (repository as? CachedPlayerRepository<V>)?.takeIf { it.cacheEnabled }
            if (cached != null) {
                if (fromCache) cached.persistCached(uuid) else cached.writeCached(uuid, value)
            }
            repository.put(uuid, value)
        }
    }

    private suspend fun <V : Any> readHot(repository: PlayerRepository<V>, uuid: UUID): Loaded<V>? {
        val cached = (repository as? CachedPlayerRepository<V>)?.takeIf { it.cacheEnabled } ?: return null
        return cached.readCached(uuid)?.let { Loaded(repository, it, true) }
    }

    private suspend fun <V : Any> loadCold(repository: PlayerRepository<V>, player: Player): Loaded<V> =
        Loaded(repository, repository.load(player), false)

    private fun onOwnershipLost(session: PlayerSession) {
        val player = session.player
        plugin.launch(Dispatchers.BukkitMain) {
            if (player.isOnline) player.kickPlayer("다른 서버가 데이터를 가져갔습니다")
        }
    }

    private suspend fun kick(player: Player, message: String) {
        withContext(Dispatchers.BukkitMain) {
            if (player.isOnline) player.kickPlayer(message)
        }
    }
}
