package kr.hqservice.framework.database.repository.player.lock

import kotlinx.coroutines.CompletableDeferred
import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Bean
class SwitchGate {
    private val waiters = ConcurrentHashMap<UUID, CompletableDeferred<Unit>>()

    fun ensure(id: UUID): CompletableDeferred<Unit> = waiters.computeIfAbsent(id) { CompletableDeferred() }

    fun signal(id: UUID) {
        ensure(id).complete(Unit)
    }

    fun reset(id: UUID) {
        waiters[id] = CompletableDeferred()
    }

    fun release(id: UUID) {
        waiters.remove(id)
    }

    fun release(id: UUID, gate: CompletableDeferred<Unit>) = waiters.remove(id, gate)

    fun clear() {
        waiters.values.forEach(CompletableDeferred<*>::cancel)
        waiters.clear()
    }

    fun isEmpty(): Boolean = waiters.isEmpty()
}
