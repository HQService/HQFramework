package kr.hqservice.framework.database.repository.player.session

import kr.hqservice.framework.database.redis.PubSubTransport
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.cache.OwnerFence
import kr.hqservice.framework.database.repository.player.session.redis.SessionStore
import java.time.Duration
import java.util.UUID

class RedisSessionCoordinator(
    private val store: SessionStore,
    private val settings: RedisSettings,
    lease: Duration,
    override val serverId: String,
    private val transport: PubSubTransport,
) : SessionCoordinator {
    override val commitsInsideTransaction: Boolean = false
    private val leaseMillis = lease.toMillis()
    private val releasedChannel = settings.key("session-released")

    override suspend fun acquire(uuid: UUID): AcquireResult = store.acquire(key(uuid), serverId, leaseMillis)

    override suspend fun renew(uuids: Collection<UUID>) {
        if (uuids.isEmpty()) return
        store.renew(uuids.map(::key), serverId, leaseMillis)
    }

    override suspend fun commit(uuid: UUID, expectedVersion: Long): Long? =
        store.commit(key(uuid), serverId, expectedVersion, leaseMillis)

    override suspend fun release(uuid: UUID): Boolean =
        store.release(key(uuid), serverId).also { released ->
            if (released) transport.publish(releasedChannel, uuid.toString().toByteArray())
        }

    override suspend fun verify(uuid: UUID, expectedVersion: Long): Boolean =
        store.verify(key(uuid), serverId, expectedVersion)

    override suspend fun ownedVersion(uuid: UUID): Long? =
        store.ownedVersion(key(uuid), serverId)

    override fun onReleased(listener: (UUID) -> Unit): AutoCloseable =
        transport.subscribe(releasedChannel) { payload ->
            runCatching { UUID.fromString(payload.decodeToString()) }.getOrNull()?.let(listener)
        }

    fun fence(uuid: UUID): OwnerFence = OwnerFence(key(uuid), serverId)

    private fun key(uuid: UUID): String = settings.key("session", uuid.toString())
}
