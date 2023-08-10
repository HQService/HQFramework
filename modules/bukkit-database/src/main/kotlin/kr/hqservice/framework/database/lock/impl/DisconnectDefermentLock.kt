package kr.hqservice.framework.database.lock.impl

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.coroutine.element.UUIDCoroutineContextElement
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.coroutine.extension.coroutineContext
import kr.hqservice.framework.database.lock.DefermentLock
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Singleton
import org.bukkit.entity.Player
import org.koin.core.annotation.Named
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

@Named("disconnect")
@Singleton(binds = [DefermentLock::class])
@Component
class DisconnectDefermentLock(plugin: HQBukkitPlugin, logger: Logger) : HQCoroutineScope(plugin, Dispatchers.IO),
    DefermentLock {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.log(Level.SEVERE, throwable) {
            "DisconnectDefermentCoroutine 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                    "job: ${context.job} \n" +
                    "stackTrace 를 출력합니다. \n"
        }
    }
    private val coroutineName = CoroutineName("DisconnectDefermentCoroutine")

    override fun getExceptionHandler(): CoroutineExceptionHandler {
        return exceptionHandler
    }

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