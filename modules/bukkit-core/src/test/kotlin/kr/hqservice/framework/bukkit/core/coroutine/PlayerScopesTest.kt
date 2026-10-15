package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Executors
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

    @Test
    fun `next job for the same id starts only after the previous one finished across suspension`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        val events = Collections.synchronizedList(mutableListOf<String>())
        val first = scopes.launch(id) { events += "first-start"; delay(100); events += "first-end" }
        val second = scopes.launch(id) { events += "second-start"; events += "second-end" }
        joinAll(first, second)
        assertEquals(listOf("first-start", "first-end", "second-start", "second-end"), events.toList())
    }

    @Test
    fun `jobs for the same id run in launch order when launched from many threads`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val id = UUID.randomUUID()
        val lock = Any()
        var sequence = 0
        val recorded = Collections.synchronizedList(mutableListOf<Int>())
        val pool = Executors.newFixedThreadPool(4)
        val jobs = Collections.synchronizedList(mutableListOf<Job>())
        val futures = (1..100).map {
            pool.submit {
                synchronized(lock) {
                    val number = ++sequence
                    jobs += scopes.launch(id) { yield(); if (number % 2 == 0) delay(1); recorded += number }
                }
            }
        }
        futures.forEach { it.get() }
        pool.shutdown()
        jobs.toList().joinAll()
        assertEquals(100, recorded.size)
        assertEquals((1..100).toList(), recorded.toList())
    }

    @Test
    fun `jobs for different ids still run in parallel`() = runBlocking {
        val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val scopes = PlayerScopes(parent)
        val started = System.nanoTime()
        joinAll(
            scopes.launch(UUID.randomUUID()) { delay(200) },
            scopes.launch(UUID.randomUUID()) { delay(200) },
        )
        val elapsed = (System.nanoTime() - started) / 1_000_000
        assertTrue(elapsed < 350, "elapsed ${elapsed}ms")
    }
}
