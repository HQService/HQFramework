package kr.hqservice.framework.database.repository.player.lifecycle

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.coroutineContext
import kr.hqservice.framework.database.repository.player.FlushRequester
import kr.hqservice.framework.database.repository.player.PendingSave
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.SavePolicy
import kr.hqservice.framework.database.repository.player.session.AcquireResult
import kr.hqservice.framework.database.repository.player.session.DatabaseSessionCoordinator
import kr.hqservice.framework.database.repository.player.session.PlayerSessionTable
import kr.hqservice.framework.database.repository.player.session.SessionCoordinator
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

class FlushSchedulerTest {
    class Counter(var n: Int)

    class CounterRepository(
        policy: SavePolicy = SavePolicy.periodic(),
        private val useFingerprint: Boolean = false,
        @Volatile var failuresLeft: Int = 0,
        private val saveDelayMillis: Long = 0,
    ) : PlayerRepository<Counter>(policy) {
        val saves = ConcurrentHashMap<UUID, AtomicInteger>()
        val savedValues: MutableList<Int> = Collections.synchronizedList(mutableListOf())

        fun saveCount(uuid: UUID): Int = saves[uuid]?.get() ?: 0

        override suspend fun load(player: Player): Counter = Counter(0)

        override suspend fun save(player: Player, value: Counter) {
            if (failuresLeft > 0) {
                failuresLeft--
                throw IllegalStateException("save failed")
            }
            if (saveDelayMillis > 0) delay(saveDelayMillis)
            savedValues += value.n
            saves.computeIfAbsent(player.uniqueId) { AtomicInteger() }.incrementAndGet()
        }

        override fun fingerprint(value: Counter): Any? = if (useFingerprint) value.n else null
    }

    object FlushProbeTable : Table("flush_probe") {
        val uuid = uuid("uuid")
    }

    class ProbeRepository : PlayerRepository<Counter>() {
        override suspend fun load(player: Player): Counter = Counter(0)

        override suspend fun save(player: Player, value: Counter) {
            FlushProbeTable.insert { it[uuid] = player.uniqueId }
        }
    }

    class FakeCoordinator : SessionCoordinator {
        override val serverId: String = "test"
        override val commitsInsideTransaction: Boolean = true
        val commits: MutableList<Pair<UUID, Long>> = Collections.synchronizedList(mutableListOf())
        val renewals: MutableList<List<UUID>> = Collections.synchronizedList(mutableListOf())
        val releases: MutableList<UUID> = Collections.synchronizedList(mutableListOf())

        override suspend fun acquire(uuid: UUID): AcquireResult = AcquireResult.Acquired(0)

        override suspend fun renew(uuids: Collection<UUID>) {
            renewals += uuids.toList()
        }

        override suspend fun commit(uuid: UUID, expectedVersion: Long): Long {
            commits += uuid to expectedVersion
            return expectedVersion + 1
        }

        override suspend fun verify(uuid: UUID, expectedVersion: Long): Boolean = true

        @Volatile var failRelease = false

        override suspend fun release(uuid: UUID): Boolean {
            if (failRelease) throw IllegalStateException("release failed")
            releases += uuid
            return true
        }
    }

    class OutsideTransactionCoordinator(
        private val events: MutableList<String>,
        private val verified: Boolean = true,
        private val result: (Long) -> Long? = { it + 1 },
    ) : SessionCoordinator {
        override val serverId: String = "test"
        override val commitsInsideTransaction: Boolean = false
        val commits: MutableList<Long> = Collections.synchronizedList(mutableListOf())

        override suspend fun acquire(uuid: UUID): AcquireResult = AcquireResult.Acquired(0)

        override suspend fun renew(uuids: Collection<UUID>) {}

        override suspend fun commit(uuid: UUID, expectedVersion: Long): Long? {
            events += if (TransactionManager.currentOrNull() == null) "commit" else "commit inside transaction"
            commits += expectedVersion
            return result(expectedVersion)
        }

        override suspend fun release(uuid: UUID): Boolean = true

        override suspend fun verify(uuid: UUID, expectedVersion: Long): Boolean {
            events += if (TransactionManager.currentOrNull() == null) "verify" else "verify inside transaction"
            return verified
        }
    }

    class RecordingRepository(
        private val events: MutableList<String>,
        @Volatile var failSave: Boolean = false,
    ) : PlayerRepository<Counter>() {
        val persisted: MutableList<Pair<UUID, Boolean>> = Collections.synchronizedList(mutableListOf())

        override suspend fun load(player: Player): Counter = Counter(0)

        override suspend fun save(player: Player, value: Counter) {
            if (failSave) throw IllegalStateException("db down")
            events += if (TransactionManager.currentOrNull() != null) "save" else "save outside transaction"
        }

        override suspend fun afterPersisted(uuid: UUID, saved: PendingSave<Counter>, offline: Boolean) {
            persisted += uuid to offline
        }
    }

    private val settings = PlayerDataSettings(
        "database",
        Duration.ofSeconds(30),
        Duration.ofSeconds(10),
        Duration.ofSeconds(5),
        Duration.ofMillis(200),
        Duration.ofSeconds(5),
        Duration.ofSeconds(60),
    )

    private lateinit var db: Database
    private val parent = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val playerScopes = PlayerScopes(parent)
    private val registry = PlayerSessionRegistry()
    private val coordinator = FakeCoordinator()
    private val logger = mockk<Logger>(relaxed = true)

    @BeforeEach
    fun setUp() {
        db = Database.connect("jdbc:h2:mem:flush;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }

    @AfterEach
    fun tearDown() {
        parent.cancel()
    }

    private fun scheduler(
        vararg repositories: PlayerRepository<*>,
        coordinator: SessionCoordinator = this.coordinator,
        settings: PlayerDataSettings = this.settings,
        onOwnershipLost: suspend (PlayerSession) -> Unit = {},
    ) = FlushScheduler(registry, { repositories.toList() }, coordinator, db, settings, playerScopes, logger, onOwnershipLost)

    private fun player(uuid: UUID, online: Boolean = true): Player = mockk<Player>(relaxed = true).also {
        every { it.uniqueId } returns uuid
        every { it.isOnline } returns online
    }

    private fun addSessions(count: Int, vararg repositories: PlayerRepository<Counter>): List<PlayerSession> =
        List(count) {
            val uuid = UUID.randomUUID()
            repositories.forEach { repository -> repository.put(uuid, Counter(0)) }
            PlayerSession(uuid, player(uuid), 0).also(registry::put)
        }.sortedBy { it.uuid }

    @Test
    fun `full flush is spread round robin across the cycle`() = runBlocking {
        val repository = CounterRepository()
        val sessions = addSessions(24, repository)
        val scheduler = scheduler(repository)

        scheduler.tick().joinAll()
        assertEquals(2, sessions.count { repository.saveCount(it.uuid) == 1 })

        repeat(11) { scheduler.tick().joinAll() }
        assertTrue(sessions.all { repository.saveCount(it.uuid) == 1 })

        scheduler.tick().joinAll()
        assertEquals(2, repository.saveCount(sessions.first().uuid))
    }

    @Test
    fun `dirty player outside the slice is saved and committed with its version`() = runBlocking {
        val repository = CounterRepository()
        val sessions = addSessions(24, repository)
        val target = sessions.last()
        target.version = 7
        repository.update(target.uuid) { it.n++ }

        scheduler(repository).tick().joinAll()

        assertEquals(1, repository.saveCount(target.uuid))
        assertFalse(repository.isDirty(target.uuid))
        assertEquals(listOf(target.uuid to 7L), coordinator.commits.filter { it.first == target.uuid })
        assertEquals(8L, target.version)
    }

    @Test
    fun `fingerprint repository skips unchanged values in the full slice`() = runBlocking {
        val repository = CounterRepository(useFingerprint = true)
        val session = addSessions(1, repository).single()
        val scheduler = scheduler(repository)

        scheduler.tick().joinAll()
        assertEquals(0, repository.saveCount(session.uuid))
        assertTrue(coordinator.commits.isEmpty())

        repository[session.uuid]!!.n = 9
        scheduler.tick().joinAll()
        assertEquals(1, repository.saveCount(session.uuid))

        repository.update(session.uuid) { it.n = 10 }
        scheduler.tick().joinAll()
        assertEquals(2, repository.saveCount(session.uuid))
    }

    @Test
    fun `on quit only repository is never saved by tick`() = runBlocking {
        val repository = CounterRepository(SavePolicy.onQuitOnly())
        val session = addSessions(2, repository).first()
        repository.update(session.uuid) { it.n++ }
        val scheduler = scheduler(repository)

        repeat(20) { scheduler.tick().joinAll() }
        assertEquals(0, repository.saveCount(session.uuid))

        assertTrue(scheduler.flushPlayer(session.uuid, listOf(repository), FlushReason.QUIT))
        assertEquals(1, repository.saveCount(session.uuid))
    }

    @Test
    fun `repeated save failures escalate to severe and keep entry dirty`() = runBlocking {
        val repository = CounterRepository(failuresLeft = 3)
        val session = addSessions(1, repository).single()
        repository.update(session.uuid) { it.n++ }
        val scheduler = scheduler(repository)

        repeat(3) { assertFalse(scheduler.flushPlayer(session.uuid, listOf(repository), FlushReason.DIRTY)) }

        verify(exactly = 2) { logger.log(Level.WARNING, any<String>(), any<Throwable>()) }
        verify(exactly = 1) { logger.log(Level.SEVERE, any<String>(), any<Throwable>()) }
        assertTrue(repository.isDirty(session.uuid))
        assertTrue(coordinator.commits.isEmpty())
        assertEquals(3, session.failures.get())
    }

    @Test
    fun `version conflict rolls back save and drops ownership`() = runBlocking {
        transaction(db) {
            SchemaUtils.drop(PlayerSessionTable, FlushProbeTable)
            SchemaUtils.create(PlayerSessionTable, FlushProbeTable)
        }
        val repository = ProbeRepository()
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        val session = PlayerSession(uuid, player(uuid), 0).also(registry::put)
        DatabaseSessionCoordinator(db, "25566", Duration.ofSeconds(30)).acquire(uuid)
        val lost = mutableListOf<PlayerSession>()
        val scheduler = scheduler(
            repository,
            coordinator = DatabaseSessionCoordinator(db, "25565", Duration.ofSeconds(30)),
            onOwnershipLost = { lost += it },
        )

        assertFalse(scheduler.flushPlayer(uuid, listOf(repository), FlushReason.QUIT))

        assertEquals(0L, transaction(db) { FlushProbeTable.selectAll().count() })
        assertEquals(1, lost.size)
        assertSame(session, lost.single())
        assertNull(registry.get(uuid))
        assertFalse(repository.contains(uuid))
    }

    @Test
    fun `successful save of an offline player releases ownership and clears it`() = runBlocking {
        val repository = CounterRepository()
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        registry.put(PlayerSession(uuid, player(uuid, online = false), 0))
        repository.update(uuid) { it.n++ }

        assertTrue(scheduler(repository).flushPlayer(uuid, listOf(repository), FlushReason.DIRTY))

        assertEquals(listOf(uuid), coordinator.releases.toList())
        assertNull(registry.get(uuid))
        assertFalse(repository.contains(uuid))
    }

    @Test
    fun `successful save of an online player keeps ownership`() = runBlocking {
        val repository = CounterRepository()
        val session = addSessions(1, repository).single()
        repository.update(session.uuid) { it.n++ }

        assertTrue(scheduler(repository).flushPlayer(session.uuid, listOf(repository), FlushReason.DIRTY))

        assertTrue(coordinator.releases.isEmpty())
        assertSame(session, registry.get(session.uuid))
        assertTrue(repository.contains(session.uuid))
    }

    @Test
    fun `renew is chunked by 500`() = runBlocking {
        val shared = mockk<Player>(relaxed = true)
        every { shared.isOnline } returns true
        repeat(1200) { registry.put(PlayerSession(UUID.randomUUID(), shared, 0)) }

        scheduler().renewAll()

        assertEquals(listOf(500, 500, 200), coordinator.renewals.map { it.size })
    }

    @Test
    fun `offline session is not renewed`() = runBlocking {
        val online = addSessions(1).single()
        val offlineId = UUID.randomUUID()
        registry.put(PlayerSession(offlineId, player(offlineId, online = false), 0))

        scheduler().renewAll()

        assertEquals(listOf(online.uuid), coordinator.renewals.flatten())
    }

    @Test
    fun `offline session is retried with quit every tick and released after a successful retry`() = runBlocking {
        val quitOnly = CounterRepository(SavePolicy.onQuitOnly(), failuresLeft = 2)
        val periodic = CounterRepository()
        val uuid = UUID.randomUUID()
        quitOnly.put(uuid, Counter(0))
        periodic.put(uuid, Counter(0))
        registry.put(PlayerSession(uuid, player(uuid, online = false), 0))
        val scheduler = scheduler(quitOnly, periodic)

        repeat(2) {
            scheduler.tick().joinAll()
            assertTrue(coordinator.releases.isEmpty())
            assertTrue(quitOnly.contains(uuid))
        }
        assertEquals(0, quitOnly.saveCount(uuid))

        scheduler.tick().joinAll()

        assertEquals(1, quitOnly.saveCount(uuid))
        assertEquals(1, periodic.saveCount(uuid))
        assertEquals(listOf(uuid), coordinator.releases.toList())
        assertNull(registry.get(uuid))
        assertFalse(quitOnly.contains(uuid))
        assertFalse(periodic.contains(uuid))
    }

    @Test
    fun `cancelling started job stops both loops`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val job = scheduler().start(scope)

        job.cancel()
        withTimeout(1000) { job.join() }

        assertTrue(job.isCompleted)
        assertTrue(job.children.none())
        scope.cancel()
    }

    private fun realCoordinator(uuid: UUID): Pair<DatabaseSessionCoordinator, Long> {
        transaction(db) {
            SchemaUtils.drop(PlayerSessionTable)
            SchemaUtils.create(PlayerSessionTable)
        }
        val coordinator = DatabaseSessionCoordinator(db, "25565", Duration.ofSeconds(30))
        val version = runBlocking { (coordinator.acquire(uuid) as AcquireResult.Acquired).version }
        return coordinator to version
    }

    @Test
    fun `concurrent explicit flushes of one player both commit without losing ownership`() = runBlocking {
        val uuid = UUID.randomUUID()
        val (real, version) = realCoordinator(uuid)
        val repoA = CounterRepository(saveDelayMillis = 50)
        val repoB = CounterRepository(saveDelayMillis = 50)
        listOf(repoA, repoB).forEach { it.put(uuid, Counter(0)); it.update(uuid) { counter -> counter.n++ } }
        val session = PlayerSession(uuid, player(uuid), version).also(registry::put)
        val lost = Collections.synchronizedList(mutableListOf<PlayerSession>())
        val scheduler = scheduler(repoA, repoB, coordinator = real, onOwnershipLost = { lost += it })

        val results = listOf(repoA, repoB).map { repository ->
            async(Dispatchers.Default) {
                var saved = false
                playerScopes.launch(uuid) { saved = scheduler.flushPlayer(uuid, listOf(repository), FlushReason.EXPLICIT) }.join()
                saved
            }
        }.awaitAll()

        assertEquals(listOf(true, true), results)
        assertEquals(2L, session.version)
        assertTrue(lost.isEmpty())
        assertEquals(1, repoA.saveCount(uuid))
        assertEquals(1, repoB.saveCount(uuid))
        assertSame(session, registry.get(uuid))
    }

    @Test
    fun `immediate updates from two threads are saved in update order`() = runBlocking {
        val uuid = UUID.randomUUID()
        val (real, version) = realCoordinator(uuid)
        val repository = CounterRepository(saveDelayMillis = 1)
        repository.put(uuid, Counter(0))
        val session = PlayerSession(uuid, player(uuid), version).also(registry::put)
        val scheduler = scheduler(repository, coordinator = real)
        val flushScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        repository.flushScope = flushScope
        repository.flushRequester = FlushRequester { id, target ->
            var saved = false
            playerScopes.launch(id) { saved = scheduler.flushPlayer(id, listOf(target), FlushReason.EXPLICIT) }.join()
            saved
        }

        val pool = Executors.newFixedThreadPool(2)
        repeat(2) { pool.submit { repeat(50) { repository.update(uuid, immediate = true) { it.n++ } } } }
        pool.shutdown()
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS))
        withTimeout(10_000) { flushScope.coroutineContext.job.children.toList().joinAll() }
        playerScopes.awaitIdle(uuid)

        val saved = repository.savedValues.toList()
        assertEquals(saved.sorted(), saved)
        assertEquals(100, saved.last())
        assertEquals(version + saved.size, session.version)
        assertSame(session, registry.get(uuid))
        flushScope.cancel()
    }

    private val fastSettings = PlayerDataSettings(
        "database",
        Duration.ofSeconds(30),
        Duration.ofMillis(50),
        Duration.ofSeconds(5),
        Duration.ofMillis(200),
        Duration.ofMillis(50),
        Duration.ofSeconds(60),
    )

    @Test
    fun `tick loop survives an exception in one iteration`() = runBlocking {
        val calls = AtomicInteger()
        val scheduler = FlushScheduler(
            registry,
            { if (calls.incrementAndGet() == 1) throw IllegalStateException("boom") else emptyList() },
            coordinator, db, fastSettings, playerScopes, logger,
        )
        val job = scheduler.start(parent)

        withTimeout(2000) { while (calls.get() < 3) delay(10) }

        job.cancel()
        verify { logger.log(Level.SEVERE, any<String>(), any<Throwable>()) }
    }

    @Test
    fun `renew loop survives an exception in one iteration`() = runBlocking {
        val uuid = UUID.randomUUID()
        val flaky = mockk<Player>(relaxed = true)
        val checks = AtomicInteger()
        every { flaky.uniqueId } returns uuid
        every { flaky.isOnline } answers { if (checks.incrementAndGet() <= 2) throw IllegalStateException("boom") else true }
        registry.put(PlayerSession(uuid, flaky, 0))
        val job = scheduler(settings = fastSettings).start(parent)

        withTimeout(2000) { while (coordinator.renewals.isEmpty()) delay(10) }

        job.cancel()
        assertEquals(listOf(uuid), coordinator.renewals.first())
    }

    @Test
    fun `started job is cancelled at plugin teardown`() {
        val job = scheduler().start(parent)

        assertEquals(true, job.coroutineContext[TeardownOptionCoroutineContextElement]?.cancelWhenPluginTeardown)
        job.cancel()
    }

    @Test
    fun `offline session with nothing to save is released on quit`() = runBlocking {
        val repository = CounterRepository()
        val uuid = UUID.randomUUID()
        registry.put(PlayerSession(uuid, player(uuid, online = false), 0))

        assertTrue(scheduler(repository).flushPlayer(uuid, listOf(repository), FlushReason.QUIT))

        assertEquals(listOf(uuid), coordinator.releases.toList())
        assertNull(registry.get(uuid))
    }

    @Test
    fun `offline session is released by tick even when no repository holds it`() = runBlocking {
        val uuid = UUID.randomUUID()
        registry.put(PlayerSession(uuid, player(uuid, online = false), 0))

        scheduler(CounterRepository()).tick().joinAll()

        assertEquals(listOf(uuid), coordinator.releases.toList())
        assertNull(registry.get(uuid))
    }

    @Test
    fun `teardown flush of one repository keeps an offline session while another repository is dirty`() = runBlocking {
        val repoA = CounterRepository()
        val repoB = CounterRepository()
        val uuid = UUID.randomUUID()
        listOf(repoA, repoB).forEach { it.put(uuid, Counter(0)); it.update(uuid) { counter -> counter.n++ } }
        registry.put(PlayerSession(uuid, player(uuid, online = false), 0))
        val scheduler = scheduler(repoA, repoB)

        assertTrue(scheduler.flushPlayer(uuid, listOf(repoA), FlushReason.TEARDOWN))

        assertTrue(coordinator.releases.isEmpty())
        assertTrue(repoB.contains(uuid))
        assertTrue(repoB.isDirty(uuid))

        assertTrue(scheduler.flushPlayer(uuid, listOf(repoB), FlushReason.TEARDOWN))

        assertEquals(listOf(uuid), coordinator.releases.toList())
        assertFalse(repoB.contains(uuid))
    }

    @Test
    fun `release failure after a committed save is logged without counting as a save failure`() = runBlocking {
        val repository = CounterRepository()
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        val session = PlayerSession(uuid, player(uuid, online = false), 0).also(registry::put)
        coordinator.failRelease = true

        assertTrue(scheduler(repository).flushPlayer(uuid, listOf(repository), FlushReason.QUIT))

        assertEquals(0, session.failures.get())
        assertEquals(1L, session.version)
        assertSame(session, registry.get(uuid))
        verify { logger.log(Level.WARNING, match<String> { it.contains("release") }, any<Throwable>()) }
    }

    @Test
    fun `coordinator committing outside the transaction commits after the saves are committed`() = runBlocking {
        val events = Collections.synchronizedList(mutableListOf<String>())
        val outside = OutsideTransactionCoordinator(events)
        val repository = RecordingRepository(events)
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        repository.update(uuid) { it.n++ }
        val session = PlayerSession(uuid, player(uuid), 4).also(registry::put)

        assertTrue(scheduler(repository, coordinator = outside).flushPlayer(uuid, listOf(repository), FlushReason.DIRTY))

        assertEquals(listOf("verify", "save", "commit"), events.toList())
        assertEquals(listOf(4L), outside.commits.toList())
        assertEquals(5L, session.version)
        assertFalse(repository.isDirty(uuid))
    }

    @Test
    fun `database failure with an outside coordinator never commits`() = runBlocking {
        val events = Collections.synchronizedList(mutableListOf<String>())
        val outside = OutsideTransactionCoordinator(events)
        val repository = RecordingRepository(events, failSave = true)
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        repository.update(uuid) { it.n++ }
        val session = PlayerSession(uuid, player(uuid), 4).also(registry::put)

        assertFalse(scheduler(repository, coordinator = outside).flushPlayer(uuid, listOf(repository), FlushReason.DIRTY))

        assertTrue(outside.commits.isEmpty())
        assertEquals(4L, session.version)
        assertTrue(repository.isDirty(uuid))
        assertEquals(1, session.failures.get())
    }

    @Test
    fun `outside commit rejected after a successful save drops ownership`() = runBlocking {
        val events = Collections.synchronizedList(mutableListOf<String>())
        val outside = OutsideTransactionCoordinator(events, result = { null })
        val repository = RecordingRepository(events)
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        repository.update(uuid) { it.n++ }
        val session = PlayerSession(uuid, player(uuid), 4).also(registry::put)
        val lost = mutableListOf<PlayerSession>()

        assertFalse(scheduler(repository, coordinator = outside, onOwnershipLost = { lost += it }).flushPlayer(uuid, listOf(repository), FlushReason.DIRTY))

        assertEquals(listOf("verify", "save", "commit"), events.toList())
        assertSame(session, lost.single())
        assertNull(registry.get(uuid))
        assertFalse(repository.contains(uuid))
    }

    @Test
    fun `outside coordinator that already lost ownership never touches the database`() = runBlocking {
        val events = Collections.synchronizedList(mutableListOf<String>())
        val outside = OutsideTransactionCoordinator(events, verified = false)
        val repository = RecordingRepository(events)
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        repository.update(uuid) { it.n++ }
        val session = PlayerSession(uuid, player(uuid), 4).also(registry::put)
        val lost = mutableListOf<PlayerSession>()

        assertFalse(scheduler(repository, coordinator = outside, onOwnershipLost = { lost += it }).flushPlayer(uuid, listOf(repository), FlushReason.DIRTY))

        assertEquals(listOf("verify"), events.toList())
        assertTrue(outside.commits.isEmpty())
        assertSame(session, lost.single())
        assertNull(registry.get(uuid))
        assertFalse(repository.contains(uuid))
        verify { logger.severe(match<String> { it.contains("ownership lost; local cache discarded (persisted data may be overwritten by the new owner)") }) }
    }

    @Test
    fun `persisted repositories are told whether the player is offline`() = runBlocking {
        val repository = RecordingRepository(mutableListOf())
        val online = UUID.randomUUID()
        val offline = UUID.randomUUID()
        val quitting = UUID.randomUUID()
        listOf(online, offline, quitting).forEach { repository.put(it, Counter(0)); repository.update(it) { counter -> counter.n++ } }
        registry.put(PlayerSession(online, player(online), 0))
        registry.put(PlayerSession(offline, player(offline, online = false), 0))
        registry.put(PlayerSession(quitting, player(quitting), 0))
        val scheduler = scheduler(repository)

        assertTrue(scheduler.flushPlayer(online, listOf(repository), FlushReason.DIRTY))
        assertTrue(scheduler.flushPlayer(offline, listOf(repository), FlushReason.DIRTY))
        assertTrue(scheduler.flushPlayer(quitting, listOf(repository), FlushReason.QUIT))

        assertEquals(listOf(online to false, offline to true, quitting to true), repository.persisted.toList())
    }

    @Test
    fun `failed save does not report the repository as persisted`() = runBlocking {
        val repository = RecordingRepository(mutableListOf(), failSave = true)
        val uuid = UUID.randomUUID()
        repository.put(uuid, Counter(0))
        repository.update(uuid) { it.n++ }
        registry.put(PlayerSession(uuid, player(uuid, online = false), 0))

        assertFalse(scheduler(repository).flushPlayer(uuid, listOf(repository), FlushReason.QUIT))

        assertTrue(repository.persisted.isEmpty())
    }
}
