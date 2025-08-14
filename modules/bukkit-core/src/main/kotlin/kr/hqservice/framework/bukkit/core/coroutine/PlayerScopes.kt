package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerScopes(
    private val parent: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val scopes = ConcurrentHashMap<UUID, CoroutineScope>()

    fun scope(id: UUID): CoroutineScope =
        scopes.computeIfAbsent(id) {
            CoroutineScope(
                parent.coroutineContext +
                        SupervisorJob(parent.coroutineContext[Job]) +
                        dispatcher.limitedParallelism(1) +
                        CoroutineName("player:$id")
            )
        }

    fun cancel(id: UUID) { scopes.remove(id)?.cancel() }
    fun cancelAll() {
        scopes.values.forEach { it.cancel() }
        scopes.clear()
    }
}