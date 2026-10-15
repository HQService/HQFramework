package kr.hqservice.framework.database.repository.player

import java.util.*

fun interface FlushRequester {
    suspend fun flush(uuid: UUID, repository: PlayerRepository<*>): Boolean
}
