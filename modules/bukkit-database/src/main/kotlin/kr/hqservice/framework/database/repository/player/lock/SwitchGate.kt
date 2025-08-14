package kr.hqservice.framework.database.repository.player.lock

import kotlinx.coroutines.CompletableDeferred
import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Bean
class SwitchGate {
    private val waiters = ConcurrentHashMap<UUID, CompletableDeferred<Unit>>()

    fun ensure(id: UUID): CompletableDeferred<Unit> =
        waiters.compute(id) { _, existing -> existing ?: CompletableDeferred() }!!

    fun signal(id: UUID) {
        val d = waiters.compute(id) { _, existing -> existing ?: CompletableDeferred() }!!
        if (!d.isCompleted) d.complete(Unit)
    }

    fun cancel(id: UUID) = waiters.remove(id)?.cancel()
    fun clear() = waiters.values.forEach(CompletableDeferred<*>::cancel)
}