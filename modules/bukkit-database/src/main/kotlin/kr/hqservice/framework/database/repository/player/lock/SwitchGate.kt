package kr.hqservice.framework.database.repository.player.lock

import kotlinx.coroutines.CompletableDeferred
import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Bean
class SwitchGate {
    private val waiters = ConcurrentHashMap<UUID, CompletableDeferred<Unit>>()

    fun ensure(id: UUID): CompletableDeferred<Unit> =
        waiters.compute(id) { _, existing -> existing?.takeIf { !it.isCompleted } ?: CompletableDeferred() }!!

    fun signal(id: UUID) {
        waiters[id]?.complete(Unit)
    }

    fun release(id: UUID) {
        waiters.remove(id)
    }

    fun cancel(id: UUID) = waiters.remove(id)?.cancel()

    fun clear() {
        waiters.values.forEach(CompletableDeferred<*>::cancel)
        waiters.clear()
    }

    fun isEmpty(): Boolean = waiters.isEmpty()
}
