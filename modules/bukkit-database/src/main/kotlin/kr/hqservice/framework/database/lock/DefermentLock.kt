package kr.hqservice.framework.database.lock

import kotlinx.coroutines.Job
import org.bukkit.entity.Player
import java.util.*

interface DefermentLock {
    suspend fun tryLock(player: Player, timedOut: Long = 3000L, whenTimedOut: suspend (Player) -> Unit) : Job

    suspend fun tryLock(playerId: UUID, timedOut: Long = 3000L, whenTimedOut: suspend (UUID) -> Unit): Job

    fun findLock(playerId: UUID): Job?

    fun unlock(playerId: UUID)
}