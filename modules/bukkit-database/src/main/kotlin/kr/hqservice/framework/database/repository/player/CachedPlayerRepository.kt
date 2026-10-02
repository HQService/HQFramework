package kr.hqservice.framework.database.repository.player

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.cache.OwnerFence
import kr.hqservice.framework.database.repository.player.cache.PlayerDataCache
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

abstract class CachedPlayerRepository<V : Any>(
    private val serializer: KSerializer<V>,
    savePolicy: SavePolicy = SavePolicy.periodic(),
) : PlayerRepository<V>(savePolicy) {
    open val cacheName: String = this::class.java.name

    internal var cache: PlayerDataCache? = null
    internal var cacheSettings: RedisSettings? = null
    internal var json: Json = Json
    internal var logger: Logger = Logger.getLogger(CachedPlayerRepository::class.java.name)
    internal var cacheWriter: ((UUID, suspend () -> Unit) -> Unit)? = null
    internal var fence: ((UUID) -> OwnerFence)? = null

    private val pendingWrites = ConcurrentHashMap.newKeySet<UUID>()
    private val fencedOut = ConcurrentHashMap.newKeySet<UUID>()

    internal val cacheEnabled: Boolean get() = cache != null

    internal fun cacheKey(uuid: UUID): String = cacheSettings!!.key("data", cacheName, uuid.toString())

    internal suspend fun readCached(uuid: UUID): V? {
        val bytes = cache?.read(cacheKey(uuid)) ?: return null
        return runCatching { json.decodeFromString(serializer, bytes.decodeToString()) }
            .onFailure { logger.log(Level.WARNING, "ignored malformed cached player data at ${cacheKey(uuid)}", it) }
            .getOrNull()
    }

    internal suspend fun writeCached(uuid: UUID, value: V, ttl: Duration? = null) {
        if (cacheEnabled) store(uuid, encode(value), ttl)
    }

    internal suspend fun persistCached(uuid: UUID) {
        cache?.persist(cacheKey(uuid))
    }

    override suspend fun peek(uuid: UUID): V? = readCached(uuid) ?: super.peek(uuid)

    override fun onMutated(uuid: UUID) {
        val writer = cacheWriter ?: return
        if (!cacheEnabled || !pendingWrites.add(uuid)) return
        try {
            writer(uuid) { writeLatest(uuid) }
        } catch (e: Exception) {
            pendingWrites.remove(uuid)
            logger.log(Level.WARNING, "failed to schedule cached player data write of $uuid", e)
        }
    }

    override suspend fun afterPersisted(uuid: UUID, saved: PendingSave<V>, offline: Boolean) {
        if (!cacheEnabled) return
        val ttl = if (offline) cacheSettings!!.dataTtl else null
        guarded("failed to write persisted player data of $uuid to the cache") { writeCached(uuid, saved.value, ttl) }
    }

    private suspend fun writeLatest(uuid: UUID) {
        pendingWrites.remove(uuid)
        guarded("failed to write cached player data of $uuid") {
            val bytes = withValue(uuid, ::encode) ?: return@guarded
            store(uuid, bytes, null)
        }
    }

    private suspend fun store(uuid: UUID, bytes: ByteArray, ttl: Duration?) {
        val cache = cache ?: return
        if (cache.write(cacheKey(uuid), bytes, ttl, fence?.invoke(uuid))) {
            fencedOut.remove(uuid)
        } else if (fencedOut.add(uuid)) {
            logger.log(Level.WARNING, "cached player data of $uuid was not written because this server no longer owns the player")
        }
    }

    private inline fun guarded(message: String, action: () -> Unit) {
        try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(Level.WARNING, message, e)
        }
    }

    private fun encode(value: V): ByteArray = json.encodeToString(serializer, value).toByteArray()
}
