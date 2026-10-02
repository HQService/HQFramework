package kr.hqservice.framework.database.repository.player.lifecycle

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
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
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

class FlushSchedulerTest {
    class Counter(var n: Int)

    class CounterRepository(
        policy: SavePolicy = SavePolicy.periodic(),
        private val useFingerprint: Boolean = false,
        @Volatile var failuresLeft: Int = 0,
    ) : PlayerRepository<Counter>(policy) {
        val saves = ConcurrentHashMap<UUID, AtomicInteger>()

        fun saveCount(uuid: UUID): Int = saves[uuid]?.get() ?: 0

        override suspend fun load(player: Player): Counter = Counter(0)

        override suspend fun save(player: Player, value: Counter) {
            if (failuresLeft > 0) {
                failuresLeft--
                throw IllegalStateException("save failed")
            }
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

        override suspend fun release(uuid: UUID): Boolean {
            releases += uuid
            return true
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
        onOwnershipLost: suspend (PlayerSession) -> Unit = {},
    ) = FlushScheduler(registry, { repositories.toList() }, coordinator, settings, playerScopes, logger, onOwnershipLost)

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
}
