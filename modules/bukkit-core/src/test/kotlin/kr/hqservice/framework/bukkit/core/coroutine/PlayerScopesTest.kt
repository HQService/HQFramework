package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerScopesTest {
    @Test
    fun `awaitIdle waits for running jobs`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        val started = CompletableDeferred<Unit>()
        var finished = false
        scopes.scope(id).launch { started.complete(Unit); delay(100); finished = true }
        started.await()
        scopes.awaitIdle(id)
        assertTrue(finished)
    }

    @Test
    fun `releaseIfIdle keeps a busy scope and removes an idle one`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        val job = scopes.scope(id).launch { delay(200) }
        scopes.releaseIfIdle(id)
        assertTrue(scopes.contains(id))
        job.join()
        scopes.releaseIfIdle(id)
        assertFalse(scopes.contains(id))
    }

    @Test
    fun `releaseIfIdle called from inside the last job keeps the scope`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        scopes.scope(id).launch { scopes.releaseIfIdle(id) }.join()
        assertTrue(scopes.contains(id))
    }

    @Test
    fun `releaseIfIdle from the completion handler of the last job removes the scope`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        val released = CompletableDeferred<Unit>()
        scopes.scope(id).launch { delay(50) }.invokeOnCompletion {
            scopes.releaseIfIdle(id)
            released.complete(Unit)
        }
        released.await()
        assertFalse(scopes.contains(id))
    }

    @Test
    fun `scope inherits the parent exception handler`() {
        var seen: Throwable? = null
        val handler = CoroutineExceptionHandler { _, t -> seen = t }
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        runBlocking {
            scopes.scope(id).launch { throw IllegalStateException("boom") }.join()
        }
        assertTrue(seen is IllegalStateException)
    }
}
