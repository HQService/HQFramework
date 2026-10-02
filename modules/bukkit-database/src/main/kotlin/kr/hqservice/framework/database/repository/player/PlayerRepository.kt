package kr.hqservice.framework.database.repository.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class PlayerRepository<V : Any>(val savePolicy: SavePolicy = SavePolicy.periodic()) : HQComponent {
    private class Entry<V>(
        @Volatile var value: V,
        @Volatile var dirtyGeneration: Long,
        @Volatile var savedGeneration: Long,
        @Volatile var lastFingerprint: Any?,
    )

    private val entries = ConcurrentHashMap<UUID, Entry<V>>()
    private val locks = ConcurrentHashMap<UUID, ReentrantLock>()

    internal var flushRequester: FlushRequester? = null
    internal var flushScope: CoroutineScope? = null

    abstract suspend fun load(player: Player): V

    abstract suspend fun save(player: Player, value: V)

    open suspend fun loadOffline(uuid: UUID): V? = null

    open fun fingerprint(value: V): Any? = null

    operator fun get(uuid: UUID): V? = entries[uuid]?.value

    operator fun set(uuid: UUID, value: V) {
        withEntryLock(uuid, Unit) { entry ->
            entry.value = value
            entry.dirtyGeneration++
        }
    }

    fun update(uuid: UUID, immediate: Boolean = false, block: (V) -> Unit): Boolean {
        val updated = withEntryLock(uuid, false) { entry ->
            block(entry.value)
            entry.dirtyGeneration++
            true
        }
        if (updated && immediate) {
            flushScope?.launch { flushRequester?.flush(uuid, this@PlayerRepository) }
        }
        return updated
    }

    suspend fun flush(uuid: UUID): Boolean = flushRequester?.flush(uuid, this) ?: false

    suspend fun peek(uuid: UUID): V? = newSuspendedTransaction(Dispatchers.IO) { loadOffline(uuid) }

    fun remove(uuid: UUID): V? {
        val removed = withLock(uuid) { entries.remove(uuid) }
        locks.remove(uuid)
        return removed?.value
    }

    fun remove(uuid: UUID, expected: V): Boolean {
        val removed = withLock(uuid) {
            val entry = entries[uuid]
            entry != null && entry.value === expected && entries.remove(uuid, entry)
        }
        if (removed) locks.remove(uuid)
        return removed
    }

    fun contains(uuid: UUID): Boolean = entries.containsKey(uuid)

    fun loadedPlayers(): Set<UUID> = entries.keys.toSet()

    internal fun put(uuid: UUID, value: V) {
        withLock(uuid) { entries[uuid] = Entry(value, 0, 0, fingerprint(value)) }
    }

    internal fun snapshot(uuid: UUID): PendingSave<V>? = withEntryLock(uuid, null) { entry ->
        val fingerprint = fingerprint(entry.value)
        PendingSave(entry.value, entry.dirtyGeneration, fingerprint, fingerprint != null && fingerprint != entry.lastFingerprint)
    }

    internal fun markSaved(uuid: UUID, saved: PendingSave<V>) {
        withEntryLock(uuid, Unit) { entry ->
            entry.savedGeneration = maxOf(entry.savedGeneration, saved.generation)
            entry.lastFingerprint = saved.fingerprint
        }
    }

    internal fun isDirty(uuid: UUID): Boolean = entries[uuid]?.let { it.dirtyGeneration > it.savedGeneration } ?: false

    internal fun dirtyPlayers(): Set<UUID> = entries.filterValues { it.dirtyGeneration > it.savedGeneration }.keys

    private inline fun <R> withLock(uuid: UUID, action: () -> R): R =
        locks.computeIfAbsent(uuid) { ReentrantLock() }.withLock(action)

    private inline fun <R> withEntryLock(uuid: UUID, missing: R, action: (Entry<V>) -> R): R {
        if (!entries.containsKey(uuid)) return missing
        return withLock(uuid) { entries[uuid]?.let(action) ?: missing }
    }
}
