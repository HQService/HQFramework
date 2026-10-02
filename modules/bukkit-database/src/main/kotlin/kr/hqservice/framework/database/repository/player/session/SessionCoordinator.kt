package kr.hqservice.framework.database.repository.player.session

import java.util.UUID

sealed interface AcquireResult {
    data class Acquired(val version: Long) : AcquireResult
    data class Held(val owner: String) : AcquireResult
}

interface SessionCoordinator {
    val serverId: String

    suspend fun acquire(uuid: UUID): AcquireResult

    suspend fun renew(uuids: Collection<UUID>)

    suspend fun commit(uuid: UUID, expectedVersion: Long): Long?

    suspend fun release(uuid: UUID): Boolean

    fun onReleased(listener: (UUID) -> Unit): AutoCloseable = AutoCloseable { }
}
