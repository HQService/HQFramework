package kr.hqservice.framework.database.repository.player

import java.util.UUID

sealed interface OfflineWriteResult {
    data object Written : OfflineWriteResult

    data class Held(val owner: String) : OfflineWriteResult
}

interface OfflineWriter {
    suspend fun <V : Any> write(uuid: UUID, repository: PlayerRepository<V>, value: V): OfflineWriteResult
}
