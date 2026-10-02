package kr.hqservice.framework.database.repository.player.lifecycle

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
import kr.hqservice.framework.database.repository.player.PendingSave
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.SavePolicy
import kr.hqservice.framework.database.repository.player.session.OwnershipLostException
import kr.hqservice.framework.database.repository.player.session.SessionCoordinator
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.roundToLong

class FlushScheduler(
    private val sessions: PlayerSessionRegistry,
    private val repositories: () -> Collection<PlayerRepository<*>>,
    private val coordinator: SessionCoordinator,
    private val settings: PlayerDataSettings,
    private val playerScopes: PlayerScopes,
    private val logger: Logger,
    private val onOwnershipLost: suspend (PlayerSession) -> Unit = {},
) {
    private class RepositoryState {
        var ticks = 0L
        var cursor = 0
    }

    private class Selected<V : Any>(val repository: PlayerRepository<V>, val snapshot: PendingSave<V>) {
        suspend fun save(player: Player) = repository.save(player, snapshot.value)

        fun markSaved(uuid: UUID) = repository.markSaved(uuid, snapshot)
    }

    private val states = ConcurrentHashMap<PlayerRepository<*>, RepositoryState>()

    fun start(scope: CoroutineScope): Job = scope.launch(Dispatchers.Default) {
        launch {
            while (isActive) {
                delay(settings.dirtyFlushInterval.toMillis())
                tick()
            }
        }
        launch {
            while (isActive) {
                delay(settings.renewInterval.toMillis())
                renewAll()
            }
        }
    }

    suspend fun flushPlayer(uuid: UUID, targets: Collection<PlayerRepository<*>>, reason: FlushReason): Boolean =
        flushPlayer(uuid, targets.associateWith { reason })

    suspend fun flushAll(reason: FlushReason) {
        sessions.all().forEach { flushPlayer(it.uuid, repositories(), reason) }
    }

    internal fun tick(): List<Job> {
        val plan = mutableMapOf<UUID, MutableMap<PlayerRepository<*>, FlushReason>>()
        val all = sessions.all()
        for (repository in repositories()) {
            val policy = repository.savePolicy as? SavePolicy.Periodic ?: continue
            val state = states.computeIfAbsent(repository) { RepositoryState() }
            val dirtyInterval = policy.dirtyInterval ?: settings.dirtyFlushInterval
            val every = maxOf(1L, (dirtyInterval.toMillis().toDouble() / settings.dirtyFlushInterval.toMillis()).roundToLong())
            if (state.ticks++ % every != 0L) continue
            repository.dirtyPlayers().forEach { plan.getOrPut(it, ::mutableMapOf).putIfAbsent(repository, FlushReason.DIRTY) }
            fullSlice(policy, dirtyInterval.toMillis(), state, all).forEach {
                plan.getOrPut(it.uuid, ::mutableMapOf)[repository] = FlushReason.FULL
            }
        }
        all.filterNot { it.player.isOnline }.forEach { session ->
            plan[session.uuid] = repositories().associateWithTo(mutableMapOf()) { FlushReason.QUIT }
        }
        return plan.map { (uuid, targets) -> playerScopes.launch(uuid) { flushPlayer(uuid, targets) } }
    }

    internal suspend fun flushPlayer(uuid: UUID, plan: Map<PlayerRepository<*>, FlushReason>): Boolean {
        val selected = plan.mapNotNull { (repository, reason) -> select(repository, uuid, reason) }
        if (selected.isEmpty()) return false
        val session = sessions.get(uuid) ?: return false
        return try {
            val next = newSuspendedTransaction(Dispatchers.IO) {
                selected.forEach { it.save(session.player) }
                coordinator.commit(uuid, session.version) ?: throw OwnershipLostException(uuid)
            }
            session.version = next
            selected.forEach { it.markSaved(uuid) }
            session.failures.set(0)
            if (!session.player.isOnline) releaseOffline(session)
            true
        } catch (e: OwnershipLostException) {
            sessions.remove(uuid)
            repositories().forEach { it.remove(uuid) }
            logger.severe("player data ownership of $uuid was lost; unsaved changes were discarded")
            onOwnershipLost(session)
            false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val failures = session.failures.incrementAndGet()
            val level = if (failures >= 3) Level.SEVERE else Level.WARNING
            logger.log(level, "failed to save player data of $uuid ($failures consecutive failures)", e)
            false
        }
    }

    private suspend fun releaseOffline(session: PlayerSession) {
        coordinator.release(session.uuid)
        sessions.remove(session.uuid)
        repositories().forEach { it.remove(session.uuid) }
    }

    internal suspend fun renewAll() {
        sessions.all().filter { it.player.isOnline }.map { it.uuid }.chunked(500).forEach { chunk ->
            try {
                coordinator.renew(chunk)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.log(Level.WARNING, "failed to renew player data leases", e)
            }
        }
    }

    private fun fullSlice(policy: SavePolicy.Periodic, dirtyMillis: Long, state: RepositoryState, all: List<PlayerSession>): List<PlayerSession> {
        if (all.isEmpty()) return emptyList()
        val fullMillis = (policy.fullInterval ?: settings.fullFlushInterval).toMillis()
        val ticksPerCycle = maxOf(1L, fullMillis / dirtyMillis)
        val batch = minOf(all.size, policy.batchSize ?: ((all.size + ticksPerCycle - 1) / ticksPerCycle).toInt())
        val start = state.cursor % all.size
        state.cursor = (start + batch) % all.size
        return List(batch) { all[(start + it) % all.size] }
    }

    private fun <V : Any> select(repository: PlayerRepository<V>, uuid: UUID, reason: FlushReason): Selected<V>? {
        val snapshot = repository.snapshot(uuid) ?: return null
        val due = when (reason) {
            FlushReason.DIRTY -> repository.isDirty(uuid)
            FlushReason.FULL -> repository.isDirty(uuid) || snapshot.fingerprint == null || snapshot.fingerprintChanged
            else -> true
        }
        return if (due) Selected(repository, snapshot) else null
    }
}
