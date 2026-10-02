package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
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
        val job = scopes.launch(id) { delay(200) }
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
        scopes.launch(id) { delay(50) }.join()
        assertFalse(scopes.contains(id))
    }

    @Test
    fun `launch from many threads for the same id never loses a job`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        val counter = AtomicInteger()
        val jobs = (1..200).map {
            async(Dispatchers.Default) { scopes.launch(id) { counter.incrementAndGet() } }
        }.awaitAll()
        jobs.joinAll()
        assertEquals(200, counter.get())
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
