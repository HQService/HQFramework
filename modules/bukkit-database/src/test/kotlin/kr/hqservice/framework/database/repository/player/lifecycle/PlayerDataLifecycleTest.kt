package kr.hqservice.framework.database.repository.player.lifecycle

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.TestPlugin
import kr.hqservice.framework.database.redis.PubSubTransport
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.impl.PlayerRepositoryRegistryImpl
import kr.hqservice.framework.database.repository.player.session.DatabaseSessionCoordinator
import kr.hqservice.framework.database.repository.player.session.PlayerSessionTable
import kr.hqservice.framework.database.repository.player.session.RedisSessionCoordinator
import kr.hqservice.framework.database.repository.player.session.SessionCoordinator
import kr.hqservice.framework.database.repository.player.session.redis.InMemorySessionStore
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Packet
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.Duration
import java.time.Instant
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

class PlayerDataLifecycleTest {
    class Points(var n: Int)

    object PointTable : Table("lifecycle_point") {
        val uuid = uuid("uuid")
        val n = integer("n")
        override val primaryKey = PrimaryKey(uuid)
    }

    class TestPointRepository(private val gate: CompletableDeferred<Unit>? = null) : PlayerRepository<Points>() {
        val loadStarted = CompletableDeferred<Unit>()
        val loads = AtomicInteger()
        val loadsFinished = AtomicInteger()

        override suspend fun load(player: Player): Points {
            loadStarted.complete(Unit)
            if (loads.incrementAndGet() == 1) gate?.await()
            val row = PointTable.selectAll().where { PointTable.uuid eq player.uniqueId }.singleOrNull()
            return Points(row?.get(PointTable.n) ?: 0).also { loadsFinished.incrementAndGet() }
        }

        override suspend fun save(player: Player, value: Points) {
            PointTable.upsert {
                it[uuid] = player.uniqueId
                it[n] = value.n
            }
        }
    }

    class FlakyRepository : PlayerRepository<Points>() {
        @Volatile var failSave = false
        @Volatile var lastSaved: Int? = null
        val loads = AtomicInteger()

        override suspend fun load(player: Player): Points {
            loads.incrementAndGet()
            return Points(0)
        }

        override suspend fun save(player: Player, value: Points) {
            if (failSave) throw IllegalStateException("db down")
            lastSaved = value.n
        }
    }

    inner class Node(
        serverId: String,
        settings: PlayerDataSettings = this@PlayerDataLifecycleTest.settings,
        val repo: TestPointRepository = TestPointRepository(),
        val coordinator: SessionCoordinator = DatabaseSessionCoordinator(db, serverId, settings.lease),
    ) {
        val registry = PlayerRepositoryRegistryImpl().also { it.register(repo) }
        val sessions = PlayerSessionRegistry()
        val loading = LoadingPlayers()
        val sent: MutableList<Packet> = Collections.synchronizedList(mutableListOf())
        val packetSender = mockk<PacketSender>().also { sender ->
            every { sender.sendPacketAll(any()) } answers { sent += firstArg<Packet>() }
        }
        val lifecycle = PlayerDataLifecycle(
            plugin,
            registry,
            sessions,
            coordinator,
            db,
            settings,
            loading,
            server.pluginManager,
            packetSender,
            mockk<HQNettyService>().also { every { it.isEnable() } returns true },
            logger,
        ).also(lifecycles::add)
    }

    class NotifyingCoordinator(private val delegate: SessionCoordinator) : SessionCoordinator by delegate {
        private val listeners = CopyOnWriteArrayList<(UUID) -> Unit>()

        override fun onReleased(listener: (UUID) -> Unit): AutoCloseable {
            listeners += listener
            return AutoCloseable { listeners -= listener }
        }

        fun fireReleased(uuid: UUID) = listeners.forEach { it(uuid) }

        fun listenerCount(): Int = listeners.size
    }

    inner class LoadedListener : Listener {
        val loaded: MutableList<UUID> = Collections.synchronizedList(mutableListOf())

        @EventHandler
        fun onLoaded(event: PlayerRepositoryLoadedEvent) {
            loaded += event.player.uniqueId
        }
    }

    private fun settings(joinTimeout: Duration = Duration.ofSeconds(1), retryInterval: Duration = Duration.ofMillis(50)) =
        PlayerDataSettings(
            "database",
            Duration.ofSeconds(30),
            Duration.ofSeconds(10),
            joinTimeout,
            retryInterval,
            Duration.ofSeconds(5),
            Duration.ofSeconds(60),
        )

    private val settings = settings()
    private val logger = Logger.getLogger("PlayerDataLifecycleTest")
    private val lifecycles = mutableListOf<PlayerDataLifecycle>()
    private val background = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var server: ServerMock
    private lateinit var plugin: TestPlugin
    private lateinit var db: Database
    private lateinit var player: PlayerMock
    private lateinit var uuid: UUID
    private lateinit var loadedListener: LoadedListener

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = TestPlugin.load(server)
        db = Database.connect("jdbc:h2:mem:lifecycle;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.drop(PlayerSessionTable, PointTable)
            SchemaUtils.create(PlayerSessionTable, PointTable)
        }
        player = server.addPlayer()
        uuid = player.uniqueId
        loadedListener = LoadedListener()
        server.pluginManager.registerEvents(loadedListener, plugin)
    }

    @AfterEach
    fun tearDown() {
        lifecycles.forEach { it.stop() }
        background.cancel()
        MockBukkit.unmock()
    }

    private fun awaitUntil(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            server.scheduler.performTicks(1)
            if (condition()) return
            Thread.sleep(10)
        }
        fail("condition not met within ${timeoutMs}ms")
    }

    private fun tickFor(millis: Long) {
        val deadline = System.currentTimeMillis() + millis
        while (System.currentTimeMillis() < deadline) {
            server.scheduler.performTicks(1)
            Thread.sleep(10)
        }
    }

    private fun owner(): String? = transaction(db) {
        PlayerSessionTable.selectAll().where { PlayerSessionTable.uuid eq uuid }.single()[PlayerSessionTable.owner]
    }

    private fun version(): Long = transaction(db) {
        PlayerSessionTable.selectAll().where { PlayerSessionTable.uuid eq uuid }.single()[PlayerSessionTable.version]
    }

    private fun storedPoints(): Int? = transaction(db) {
        PointTable.selectAll().where { PointTable.uuid eq uuid }.singleOrNull()?.get(PointTable.n)
    }

    private fun insertSession(owner: String, leaseUntil: Instant) = transaction(db) {
        PlayerSessionTable.insert {
            it[PlayerSessionTable.uuid] = this@PlayerDataLifecycleTest.uuid
            it[PlayerSessionTable.owner] = owner
            it[PlayerSessionTable.version] = 3
            it[PlayerSessionTable.leaseUntil] = leaseUntil
        }
    }

    private fun joined(node: Node) {
        node.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { node.sessions.get(uuid) != null && uuid !in node.loading }
    }

    @Test
    fun `join acquires ownership, loads and fires loaded event`() {
        val a = Node("25565")

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        assertTrue(uuid in a.loading)
        awaitUntil { loadedListener.loaded.contains(uuid) }

        assertEquals("25565", owner())
        assertEquals(0L, version())
        assertNotNull(a.repo[uuid])
        assertNotNull(a.sessions.get(uuid))
        awaitUntil { uuid !in a.loading }
    }

    @Test
    fun `quit saves, releases, clears cache and sends saved packet`() {
        val a = Node("25565")
        joined(a)
        a.repo.update(uuid) { it.n = 42 }

        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { a.sent.isNotEmpty() }

        assertEquals(42, storedPoints())
        assertNull(owner())
        assertFalse(a.repo.contains(uuid))
        assertNull(a.sessions.get(uuid))
        assertEquals(uuid, (a.sent.single() as PlayerDataSavedPacket).id)
    }

    @Test
    fun `switching server waits for the previous owner to release`() {
        val a = Node("25565")
        val b = Node("25566")
        joined(a)
        a.repo.update(uuid) { it.n = 7 }

        b.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        tickFor(100)
        assertFalse(b.repo.contains(uuid))

        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { b.sessions.get(uuid) != null }

        assertEquals("25566", owner())
        assertEquals(7, b.repo[uuid]!!.n)
    }

    @Test
    fun `saved packet hint retries acquire before the next poll`() {
        val a = Node("25565")
        val b = Node("25566", settings(joinTimeout = Duration.ofSeconds(10), retryInterval = Duration.ofSeconds(2)))
        joined(a)
        a.repo.update(uuid) { it.n = 9 }

        b.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        tickFor(100)
        assertFalse(b.repo.contains(uuid))
        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { a.sent.isNotEmpty() }

        b.lifecycle.onPacketReceive(AsyncNettyPacketReceivedEvent(mockk(relaxed = true), PlayerDataSavedPacket(uuid)))
        awaitUntil(500) { b.sessions.get(uuid) != null }

        assertEquals(9, b.repo[uuid]!!.n)
    }

    @Test
    fun `coordinator release notification retries acquire before the next poll`() {
        val a = Node("25565")
        val slow = settings(joinTimeout = Duration.ofSeconds(10), retryInterval = Duration.ofSeconds(2))
        val notifying = NotifyingCoordinator(DatabaseSessionCoordinator(db, "25566", slow.lease))
        val b = Node("25566", slow, coordinator = notifying)
        joined(a)
        a.repo.update(uuid) { it.n = 4 }

        b.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        tickFor(100)
        assertFalse(b.repo.contains(uuid))
        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { a.sent.isNotEmpty() }

        notifying.fireReleased(uuid)
        awaitUntil(500) { b.sessions.get(uuid) != null }

        assertEquals(4, b.repo[uuid]!!.n)
    }

    @Test
    fun `shutdown closes the release notification subscription`() {
        val notifying = NotifyingCoordinator(DatabaseSessionCoordinator(db, "25565", settings.lease))
        val a = Node("25565", coordinator = notifying)
        assertEquals(1, notifying.listenerCount())

        a.lifecycle.shutdown()

        assertEquals(0, notifying.listenerCount())
    }

    @Test
    fun `join is kicked when ownership is not released before the timeout`() {
        insertSession("25567", Instant.now().plusSeconds(600))
        val b = Node("25566")
        val started = System.currentTimeMillis()

        b.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { !player.isOnline }

        assertTrue(System.currentTimeMillis() - started >= settings.joinTimeout.toMillis())
        assertNull(b.sessions.get(uuid))
        assertFalse(b.repo.contains(uuid))
        assertEquals("25567", owner())
        awaitUntil { uuid !in b.loading }
    }

    @Test
    fun `expired lease of a crashed owner is taken over with the last saved data`() {
        insertSession("25567", Instant.now().minusSeconds(600))
        transaction(db) {
            PointTable.insert {
                it[PointTable.uuid] = this@PlayerDataLifecycleTest.uuid
                it[n] = 5
            }
        }
        val b = Node("25566")

        joined(b)

        assertEquals("25566", owner())
        assertEquals(3L, b.sessions.get(uuid)!!.version)
        assertEquals(5, b.repo[uuid]!!.n)
    }

    @Test
    fun `late save after takeover is rejected and the player is kicked`() {
        val a = Node("25565")
        joined(a)
        transaction(db) {
            PlayerSessionTable.update({ PlayerSessionTable.uuid eq uuid }) {
                it[owner] = "25566"
                it[version] = version + 1
            }
        }
        a.repo.update(uuid) { it.n = 1 }

        val flush = background.launch { a.repo.flush(uuid) }
        awaitUntil { flush.isCompleted && !player.isOnline }

        assertFalse(a.repo.contains(uuid))
        assertNull(a.sessions.get(uuid))
        assertNotEquals(1, storedPoints())
        assertEquals("25566", owner())
    }

    @Test
    fun `player leaving during load gets no session and ownership is released`() {
        val gate = CompletableDeferred<Unit>()
        val a = Node("25565", repo = TestPointRepository(gate))

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { a.repo.loadStarted.isCompleted }
        player.disconnect()
        assertFalse(player.isOnline)
        gate.complete(Unit)
        awaitUntil { uuid !in a.loading }

        assertNull(a.sessions.get(uuid))
        assertFalse(a.repo.contains(uuid))
        assertNull(owner())
        assertTrue(loadedListener.loaded.isEmpty())
    }

    @Test
    fun `quit event during load prevents the session even while the player still looks online`() {
        val gate = CompletableDeferred<Unit>()
        val a = Node("25565", repo = TestPointRepository(gate))

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { a.repo.loadStarted.isCompleted }
        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        gate.complete(Unit)
        awaitUntil { uuid !in a.loading }
        tickFor(100)

        assertNull(a.sessions.get(uuid))
        assertFalse(a.repo.contains(uuid))
        assertNull(owner())
    }

    @Test
    fun `teardown flush saves the repository and shutdown releases ownership`() {
        val a = Node("25565")
        joined(a)
        a.repo.update(uuid) { it.n = 11 }

        runBlocking { a.lifecycle.flushRepositoryForTeardown(a.repo) }
        assertEquals(11, storedPoints())
        assertEquals("25565", owner())

        a.lifecycle.shutdown()
        assertNull(owner())
        assertNull(a.sessions.get(uuid))
    }

    @Test
    fun `rejoin after a failed quit save keeps the unsaved cache`() {
        val flaky = FlakyRepository()
        val a = Node("25565")
        a.registry.register(flaky)
        a.lifecycle.attach(flaky)
        joined(a)
        flaky.update(uuid) { it.n = 5 }
        flaky.failSave = true

        val retained = a.sessions.get(uuid)!!

        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { retained.failures.get() >= 1 }
        assertEquals("25565", owner())
        assertTrue(retained === a.sessions.get(uuid))

        flaky.failSave = false
        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { uuid !in a.loading }

        assertEquals(5, flaky[uuid]!!.n)
        assertEquals(1, flaky.loads.get())
        assertTrue(retained === a.sessions.get(uuid))
        assertTrue(runBlocking { flaky.flush(uuid) })
        assertEquals(5, flaky.lastSaved)
        assertEquals(1L, version())
        assertEquals("25565", owner())
    }

    @Test
    fun `failed quit save keeps ownership, cache and session for a retry`() {
        val failing = object : PlayerRepository<Points>() {
            override suspend fun load(player: Player): Points = Points(0)

            override suspend fun save(player: Player, value: Points) {
                throw IllegalStateException("db down")
            }
        }
        val a = Node("25565")
        a.registry.register(failing)
        joined(a)
        a.repo.update(uuid) { it.n = 3 }
        val session = a.sessions.get(uuid)!!

        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { session.failures.get() >= 1 }

        assertEquals("25565", owner())
        assertNotNull(a.sessions.get(uuid))
        assertTrue(a.repo.contains(uuid))
        assertTrue(failing.contains(uuid))
        assertTrue(a.sent.isEmpty())
    }

    @Test
    fun `superseded join does not release ownership or overwrite the cache of the newer join`() {
        val gate = CompletableDeferred<Unit>()
        val a = Node("25565", repo = TestPointRepository(gate))

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { a.repo.loadStarted.isCompleted }
        player.disconnect()
        val rejoined = PlayerMock(server, player.name, uuid).also(server::addPlayer)
        a.lifecycle.onJoin(PlayerJoinEvent(rejoined, "join"))
        awaitUntil { a.sessions.get(uuid) != null && uuid !in a.loading }
        a.repo.update(uuid) { it.n = 8 }

        gate.complete(Unit)
        awaitUntil { a.repo.loadsFinished.get() == 2 }
        tickFor(200)

        assertEquals("25565", owner())
        assertTrue(a.sessions.get(uuid)!!.player === rejoined)
        assertEquals(8, a.repo[uuid]!!.n)
        assertEquals(listOf(uuid), loadedListener.loaded.toList())
    }

    @Test
    fun `join is kicked when a previous job of the player does not finish within the join timeout`() {
        val gate = CompletableDeferred<Unit>()
        val saveStarted = CompletableDeferred<Unit>()
        val blocking = object : PlayerRepository<Points>() {
            override suspend fun load(player: Player): Points = Points(0)

            override suspend fun save(player: Player, value: Points) {
                saveStarted.complete(Unit)
                gate.await()
            }
        }
        val a = Node("25565")
        a.registry.register(blocking)
        joined(a)
        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { saveStarted.isCompleted }

        try {
            a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
            awaitUntil { !player.isOnline }
        } finally {
            gate.complete(Unit)
        }
    }

    @Test
    fun `ownership loss during a flush blocking the main thread does not wait for the main thread`() {
        val a = Node("25565")
        joined(a)
        transaction(db) {
            PlayerSessionTable.update({ PlayerSessionTable.uuid eq uuid }) {
                it[owner] = "25566"
                it[version] = version + 1
            }
        }
        a.repo.update(uuid) { it.n = 1 }

        val saved = runBlocking { withTimeout(3000) { a.repo.flush(uuid) } }

        assertFalse(saved)
        awaitUntil { !player.isOnline }
    }

    @Test
    fun `lifecycle is created even when subscribing to release notifications fails`() {
        val unreachable = object : PubSubTransport {
            override fun publish(channel: String, payload: ByteArray) {}

            override fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable =
                throw IllegalStateException("redis unreachable")
        }
        val coordinator = RedisSessionCoordinator(
            InMemorySessionStore(),
            RedisSettings("redis://localhost", "hq", Duration.ofSeconds(60)),
            settings.lease,
            "25565",
            unreachable,
        )

        val a = Node("25565", coordinator = coordinator)
        joined(a)

        assertNotNull(a.repo[uuid])
        a.lifecycle.shutdown()
    }
}
