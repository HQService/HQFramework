package kr.hqservice.framework.database.repository.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PlayerRepositoryTest {
    class Counter(var n: Int)

    class TestRepository(
        private val useFingerprint: Boolean = false,
        private val offline: Counter? = null,
    ) : PlayerRepository<Counter>() {
        override suspend fun load(player: Player): Counter = Counter(0)

        override suspend fun save(player: Player, value: Counter) {}

        override suspend fun loadOffline(uuid: UUID): Counter? = offline

        override fun fingerprint(value: Counter): Any? = if (useFingerprint) value.n else null
    }

    private class RecordingRequester : FlushRequester {
        val calls = mutableListOf<Pair<UUID, PlayerRepository<*>>>()

        override suspend fun flush(uuid: UUID, repository: PlayerRepository<*>): Boolean {
            calls += uuid to repository
            return true
        }
    }

    private val uuid = UUID.randomUUID()

    @Test
    fun `update marks dirty and markSaved clears it`() {
        val repo = TestRepository()
        repo.put(uuid, Counter(0))
        assertTrue(repo.dirtyPlayers().isEmpty())

        repo.update(uuid) { it.n++ }
        assertEquals(setOf(uuid), repo.dirtyPlayers())

        val snapshot = repo.snapshot(uuid)!!
        assertEquals(1L, snapshot.generation)
        repo.markSaved(uuid, snapshot)
        assertFalse(repo.isDirty(uuid))
    }

    @Test
    fun `update after snapshot keeps entry dirty`() {
        val repo = TestRepository()
        repo.put(uuid, Counter(0))
        repo.update(uuid) { it.n++ }
        val old = repo.snapshot(uuid)!!
        repo.update(uuid) { it.n++ }
        repo.markSaved(uuid, old)
        assertTrue(repo.isDirty(uuid))
    }

    @Test
    fun `concurrent updates are serialised`() {
        val repo = TestRepository()
        val counter = Counter(0)
        repo.put(uuid, counter)
        val pool = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        repeat(2) {
            pool.submit {
                start.await()
                repeat(500) { repo.update(uuid) { it.n++ } }
            }
        }
        start.countDown()
        pool.shutdown()
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS))
        assertEquals(1000, counter.n)
    }

    @Test
    fun `fingerprint change is detected and reset by markSaved`() {
        val repo = TestRepository(useFingerprint = true)
        repo.put(uuid, Counter(0))
        assertFalse(repo.snapshot(uuid)!!.fingerprintChanged)

        repo.update(uuid) { it.n = 5 }
        val snapshot = repo.snapshot(uuid)!!
        assertTrue(snapshot.fingerprintChanged)

        repo.markSaved(uuid, snapshot)
        assertFalse(repo.snapshot(uuid)!!.fingerprintChanged)
    }

    @Test
    fun `null fingerprint never reports change`() {
        val repo = TestRepository(useFingerprint = false)
        repo.put(uuid, Counter(0))
        assertFalse(repo.snapshot(uuid)!!.fingerprintChanged)
        repo.update(uuid) { it.n = 5 }
        assertFalse(repo.snapshot(uuid)!!.fingerprintChanged)
    }

    @Test
    fun `immediate update requests flush`() {
        val repo = TestRepository()
        val requester = RecordingRequester()
        repo.flushRequester = requester
        repo.flushScope = CoroutineScope(Dispatchers.Unconfined)
        repo.put(uuid, Counter(0))

        assertTrue(repo.update(uuid, immediate = true) { it.n++ })
        assertEquals(listOf<Pair<UUID, PlayerRepository<*>>>(uuid to repo), requester.calls)
    }

    @Test
    fun `update on unknown uuid does nothing`() {
        val repo = TestRepository()
        val requester = RecordingRequester()
        repo.flushRequester = requester
        repo.flushScope = CoroutineScope(Dispatchers.Unconfined)
        var ran = false

        assertFalse(repo.update(uuid, immediate = true) { ran = true })
        assertFalse(ran)
        assertTrue(requester.calls.isEmpty())
    }

    @Test
    fun `flush delegates to requester and is no-op without one`() = runBlocking {
        val repo = TestRepository()
        assertFalse(repo.flush(uuid))

        val requester = RecordingRequester()
        repo.flushRequester = requester
        assertTrue(repo.flush(uuid))
        assertEquals(listOf<Pair<UUID, PlayerRepository<*>>>(uuid to repo), requester.calls)
    }

    object PeekTable : Table("peek_counter") {
        val uuid = uuid("uuid")
        val n = integer("n")
    }

    class ExposedRepository : PlayerRepository<Counter>() {
        override suspend fun load(player: Player): Counter = Counter(0)

        override suspend fun save(player: Player, value: Counter) {}

        override suspend fun loadOffline(uuid: UUID): Counter? =
            PeekTable.selectAll().where { PeekTable.uuid eq uuid }.singleOrNull()?.let { Counter(it[PeekTable.n]) }
    }

    @Test
    fun `peek runs loadOffline inside a transaction`() = runBlocking {
        val db = Database.connect("jdbc:h2:mem:peek;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.drop(PeekTable)
            SchemaUtils.create(PeekTable)
            PeekTable.insert {
                it[PeekTable.uuid] = this@PlayerRepositoryTest.uuid
                it[n] = 42
            }
        }
        val repo = ExposedRepository()

        assertEquals(42, repo.peek(uuid)?.n)
        assertFalse(repo.contains(uuid))
    }

    @Test
    fun `peek reads offline value without caching`() = runBlocking {
        val offline = Counter(7)
        val repo = TestRepository(offline = offline)
        assertSame(offline, repo.peek(uuid))
        assertFalse(repo.contains(uuid))
        assertNull(TestRepository().peek(uuid))
    }

    @Test
    fun `set marks dirty and remove with expected checks identity`() {
        val repo = TestRepository()
        repo.put(uuid, Counter(0))
        val replaced = Counter(1)
        repo[uuid] = replaced
        assertTrue(repo.isDirty(uuid))
        assertSame(replaced, repo[uuid])

        val other = UUID.randomUUID()
        repo[other] = Counter(2)
        assertFalse(repo.contains(other))
        assertFalse(repo.isDirty(other))
        assertEquals(setOf(uuid), repo.loadedPlayers())

        assertFalse(repo.remove(uuid, Counter(1)))
        assertTrue(repo.contains(uuid))
        assertTrue(repo.remove(uuid, replaced))
        assertFalse(repo.contains(uuid))
        assertNull(repo[uuid])
    }
}
