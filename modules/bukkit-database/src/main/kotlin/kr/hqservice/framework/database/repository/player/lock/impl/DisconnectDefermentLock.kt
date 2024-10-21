package kr.hqservice.framework.database.repository.player.lock.impl

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.bukkit.core.coroutine.element.UUIDCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.coroutineContext
import kr.hqservice.framework.database.repository.player.lock.DefermentLock
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import org.bukkit.entity.Player
import java.util.*

@Qualifier("disconnect")
@Singleton(binds = [DefermentLock::class])
@Component
class DisconnectDefermentLock(plugin: HQBukkitPlugin) : HQCoroutineScope(plugin, Dispatchers.IO), DefermentLock {
    private val coroutineName = CoroutineName("DisconnectDefermentCoroutine")

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }

    override suspend fun tryLock(player: Player, timedOut: Long, whenTimedOut: suspend (Player) -> Unit): Job {
        return launch(UUIDCoroutineContextElement(player.uniqueId)) {
            delay(timedOut)
            whenTimedOut(player)
        }
    }

    override suspend fun tryLock(playerId: UUID, timedOut: Long, whenTimedOut: suspend (UUID) -> Unit): Job {
        return launch(UUIDCoroutineContextElement(playerId)) {
            delay(timedOut)
            whenTimedOut(playerId)
        }
    }

    override fun unlock(playerId: UUID) {
        val lock = getSupervisor().children.firstOrNull {
            it.coroutineContext[UUIDCoroutineContextElement]?.uuid == playerId
        }
        lock?.cancel()
    }

    override fun findLock(playerId: UUID): Job? {
        return getSupervisor().children.firstOrNull {
            it.coroutineContext[UUIDCoroutineContextElement]?.uuid == playerId
        }
    }
}