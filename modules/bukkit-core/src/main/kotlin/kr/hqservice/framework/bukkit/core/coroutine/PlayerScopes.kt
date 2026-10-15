package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class PlayerScopes(
    private val parent: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private class PlayerScope(val scope: CoroutineScope, val mutex: Mutex)

    private val scopes = ConcurrentHashMap<UUID, PlayerScope>()

    fun scope(id: UUID): CoroutineScope = scopes.computeIfAbsent(id, ::createScope).scope

    fun launch(id: UUID, context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit): Job {
        lateinit var job: Job
        scopes.compute(id) { _, existing ->
            val entry = existing ?: createScope(id)
            job = entry.scope.launch(context) { entry.mutex.withLock { block() } }
            entry
        }
        job.invokeOnCompletion { releaseIfIdle(id) }
        return job
    }

    private fun createScope(id: UUID): PlayerScope =
        PlayerScope(
            CoroutineScope(
                parent.coroutineContext +
                        SupervisorJob(parent.coroutineContext[Job]) +
                        dispatcher.limitedParallelism(1) +
                        CoroutineName("player:$id")
            ),
            Mutex()
        )

    suspend fun awaitIdle(id: UUID) {
        scopes[id]?.scope?.coroutineContext?.get(Job)?.children?.toList()?.joinAll()
    }

    fun releaseIfIdle(id: UUID) {
        scopes.computeIfPresent(id) { _, entry ->
            if (entry.scope.hasRunningJobs()) entry
            else {
                entry.scope.cancel()
                null
            }
        }
    }

    fun contains(id: UUID): Boolean = scopes.containsKey(id)

    fun cancel(id: UUID) { scopes.remove(id)?.scope?.cancel() }
    fun cancelAll() {
        scopes.values.forEach { it.scope.cancel() }
        scopes.clear()
    }

    private fun CoroutineScope.hasRunningJobs(): Boolean =
        coroutineContext[Job]?.children?.any() == true
}
