package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
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

    suspend fun awaitIdle(id: UUID) {
        scopes[id]?.coroutineContext?.get(Job)?.children?.toList()?.joinAll()
    }

    fun releaseIfIdle(id: UUID) {
        scopes.computeIfPresent(id) { _, scope ->
            if (scope.hasRunningJobs()) scope
            else {
                scope.cancel()
                null
            }
        }
    }

    fun contains(id: UUID): Boolean = scopes.containsKey(id)

    fun cancel(id: UUID) { scopes.remove(id)?.cancel() }
    fun cancelAll() {
        scopes.values.forEach { it.cancel() }
        scopes.clear()
    }

    private fun CoroutineScope.hasRunningJobs(): Boolean =
        coroutineContext[Job]?.children?.any() == true
}
