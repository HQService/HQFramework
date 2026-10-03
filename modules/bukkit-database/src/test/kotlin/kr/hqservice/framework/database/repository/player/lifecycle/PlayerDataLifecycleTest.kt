package kr.hqservice.framework.database.repository.player.lifecycle

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import io.lettuce.core.RedisConnectionException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.TestPlugin
import kr.hqservice.framework.database.redis.InMemoryPubSubTransport
import kr.hqservice.framework.database.redis.PubSubTransport
import kr.hqservice.framework.database.redis.RedisProvider
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.CachedPlayerRepository
import kr.hqservice.framework.database.repository.player.OfflineWriteResult
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.cache.InMemoryPlayerDataCache
import kr.hqservice.framework.database.repository.player.cache.OwnerFence
import kr.hqservice.framework.database.repository.player.cache.PlayerDataCache
import kr.hqservice.framework.database.repository.player.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.impl.PlayerRepositoryRegistryImpl
import kr.hqservice.framework.database.repository.player.session.AcquireResult
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
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
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

        override suspend fun saveOffline(uuid: UUID, value: Points) {
            PointTable.upsert {
                it[PointTable.uuid] = uuid
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

    @Serializable
    data class Wallet(var coins: Int)

    class WalletRepository : CachedPlayerRepository<Wallet>(Wallet.serializer()) {
        val loads = AtomicInteger()

        override suspend fun load(player: Player): Wallet = Wallet(100).also { loads.incrementAndGet() }

        override suspend fun save(player: Player, value: Wallet) {}

        override suspend fun saveOffline(uuid: UUID, value: Wallet) {}
    }

    inner class Node(
        serverId: String,
        settings: PlayerDataSettings = this@PlayerDataLifecycleTest.settings,
        val repo: TestPointRepository = TestPointRepository(),
        val coordinator: SessionCoordinator = DatabaseSessionCoordinator(db, serverId, settings.lease),
        redisSettings: RedisSettings = disabledRedis,
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
            redisSettings,
            RedisProvider(redisSettings, logger),
            Json,
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
    private val disabledRedis = RedisSettings("", "hq", Duration.ofSeconds(60))
    private val enabledRedis = RedisSettings("redis://localhost:1", "hq", Duration.ofSeconds(60))
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
        awaitUntil { notifying.listenerCount() == 1 }

        a.lifecycle.shutdown()

        awaitUntil { notifying.listenerCount() == 0 }
    }

    @Test
    fun `creating the lifecycle does not wait for the release notification subscription`() {
        val gate = CountDownLatch(1)
        val closed = AtomicBoolean()
        val blocking = object : SessionCoordinator by DatabaseSessionCoordinator(db, "25565", settings.lease) {
            override fun onReleased(listener: (UUID) -> Unit): AutoCloseable {
                gate.await(2, TimeUnit.SECONDS)
                return AutoCloseable { closed.set(true) }
            }
        }
        val started = System.currentTimeMillis()

        val a = Node("25565", coordinator = blocking)

        assertTrue(System.currentTimeMillis() - started < 1000)
        a.lifecycle.shutdown()
        gate.countDown()
        awaitUntil { closed.get() }
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
    fun `join retries acquire through a short redis outage and loads the player`() {
        val slow = settings(joinTimeout = Duration.ofSeconds(2), retryInterval = Duration.ofMillis(50))
        val real = DatabaseSessionCoordinator(db, "25565", slow.lease)
        val recoversAt = AtomicLong(Long.MAX_VALUE)
        val attempts = AtomicInteger()
        val flaky = object : SessionCoordinator by real {
            override suspend fun acquire(uuid: UUID): AcquireResult {
                attempts.incrementAndGet()
                if (System.currentTimeMillis() < recoversAt.get()) throw RedisConnectionException("redis down")
                return real.acquire(uuid)
            }
        }
        val a = Node("25565", slow, coordinator = flaky)
        val started = System.currentTimeMillis()
        recoversAt.set(started + 300)

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil(3000) { loadedListener.loaded.contains(uuid) }

        assertTrue(System.currentTimeMillis() - started >= 300)
        assertTrue(attempts.get() > 1)
        assertTrue(player.isOnline)
        assertEquals("25565", owner())
        assertNotNull(a.repo[uuid])
        assertNotNull(a.sessions.get(uuid))
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
    fun `rejoin discards a retained session superseded by another server`() {
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
        transaction(db) {
            PlayerSessionTable.update({ PlayerSessionTable.uuid eq uuid }) { it[version] = version + 1 }
        }

        flaky.failSave = false
        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { uuid !in a.loading }

        val session = a.sessions.get(uuid)!!
        assertNotSame(retained, session)
        assertEquals(1L, session.version)
        assertEquals(0, flaky[uuid]!!.n)
        assertEquals(2, flaky.loads.get())
        assertTrue(runBlocking { flaky.flush(uuid) })
        assertEquals(0, flaky.lastSaved)
        assertEquals(2L, version())
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
            logger,
        )

        val a = Node("25565", coordinator = coordinator)
        joined(a)

        assertNotNull(a.repo[uuid])
        a.lifecycle.shutdown()
    }

    private fun cachedNode(
        memory: PlayerDataCache,
        wallet: WalletRepository,
        redisSettings: RedisSettings = enabledRedis,
        coordinator: SessionCoordinator = DatabaseSessionCoordinator(db, "25565", settings.lease),
    ): Node =
        Node("25565", redisSettings = redisSettings, coordinator = coordinator).also { node ->
            node.lifecycle.cacheFactory = { memory }
            node.registry.register(wallet)
            node.lifecycle.attach(wallet)
        }

    private fun walletKey(wallet: WalletRepository): String = "hq:data:${wallet.cacheName}:$uuid"

    @Test
    fun `join uses the cached value instead of loading from the database`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet)
        memory.values[walletKey(wallet)] = """{"coins":7}""".toByteArray()

        joined(a)

        assertEquals(Wallet(7), wallet[uuid])
        assertEquals(0, wallet.loads.get())
    }

    @Test
    fun `join without a cached value loads from the database and populates the cache`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet)

        joined(a)

        assertEquals(Wallet(100), wallet[uuid])
        assertEquals(1, wallet.loads.get())
        assertEquals("""{"coins":100}""", memory.text(walletKey(wallet)))
    }

    @Test
    fun `updates are written to the cache and quit sets the data ttl`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet)
        joined(a)

        wallet.update(uuid) { it.coins = 42 }
        awaitUntil { memory.text(walletKey(wallet)) == """{"coins":42}""" }
        assertNull(memory.ttls[walletKey(wallet)])

        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { a.sent.isNotEmpty() }

        assertEquals(enabledRedis.dataTtl, memory.ttls[walletKey(wallet)])
        assertEquals("""{"coins":42}""", memory.text(walletKey(wallet)))
    }

    @Test
    fun `join from the cache removes the data ttl`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet)
        memory.values[walletKey(wallet)] = """{"coins":7}""".toByteArray()
        memory.ttls[walletKey(wallet)] = enabledRedis.dataTtl

        joined(a)

        assertEquals(Wallet(7), wallet[uuid])
        assertNull(memory.ttls[walletKey(wallet)])
    }

    @Test
    fun `full flush of a directly mutated value writes the persisted value to the cache`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet)
        joined(a)

        wallet[uuid]!!.coins = 33
        assertTrue(runBlocking { wallet.flush(uuid) })

        assertEquals("""{"coins":33}""", memory.text(walletKey(wallet)))
        assertNull(memory.ttls[walletKey(wallet)])
    }

    @Test
    fun `cache writes are fenced by redis session ownership`() {
        val store = InMemorySessionStore()
        val coordinator = RedisSessionCoordinator(store, enabledRedis, settings.lease, "25565", InMemoryPubSubTransport(), logger)
        val memory = InMemoryPlayerDataCache(store::owner)
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet, coordinator = coordinator)
        joined(a)
        assertEquals("""{"coins":100}""", memory.text(walletKey(wallet)))

        wallet.update(uuid) { it.coins = 1 }
        awaitUntil { memory.text(walletKey(wallet)) == """{"coins":1}""" }
        store.entry("hq:session:$uuid")!!.owner = "25566"
        wallet.update(uuid) { it.coins = 2 }
        tickFor(100)

        assertEquals("""{"coins":1}""", memory.text(walletKey(wallet)))
    }

    @Test
    fun `cache is read and written outside the database transaction`() {
        val memory = InMemoryPlayerDataCache()
        val inTransaction = CopyOnWriteArrayList<String>()
        val probing = object : PlayerDataCache by memory {
            override suspend fun read(key: String): ByteArray? =
                memory.read(key).also { if (TransactionManager.currentOrNull() != null) inTransaction += "read" }

            override suspend fun write(key: String, value: ByteArray, ttl: Duration?, fence: OwnerFence?): Boolean =
                memory.write(key, value, ttl, fence).also { if (TransactionManager.currentOrNull() != null) inTransaction += "write" }
        }
        val wallet = WalletRepository()
        val a = cachedNode(probing, wallet)

        joined(a)

        assertEquals("""{"coins":100}""", memory.text(walletKey(wallet)))
        assertTrue(inTransaction.isEmpty(), inTransaction.toString())
    }

    @Test
    fun `join is kicked and ownership released when loading does not finish within the join timeout`() {
        val memory = InMemoryPlayerDataCache()
        val hanging = object : PlayerDataCache by memory {
            override suspend fun read(key: String): ByteArray? = awaitCancellation()
        }
        val wallet = WalletRepository()
        val a = cachedNode(hanging, wallet)
        val started = System.currentTimeMillis()

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil(5000) { !player.isOnline }

        assertTrue(System.currentTimeMillis() - started >= settings.joinTimeout.toMillis())
        awaitUntil { uuid !in a.loading }
        assertNull(a.sessions.get(uuid))
        assertFalse(wallet.contains(uuid))
        assertNull(owner())
    }

    @Test
    fun `join keeps retrying a failing cache read until the join timeout and loads the player`() {
        val slow = settings(joinTimeout = Duration.ofSeconds(2), retryInterval = Duration.ofMillis(50))
        val memory = InMemoryPlayerDataCache()
        val recoversAt = AtomicLong(Long.MAX_VALUE)
        val reads = AtomicInteger()
        val flaky = object : PlayerDataCache by memory {
            override suspend fun read(key: String): ByteArray? {
                reads.incrementAndGet()
                if (System.currentTimeMillis() < recoversAt.get()) throw RedisConnectionException("redis down")
                return memory.read(key)
            }
        }
        val wallet = WalletRepository()
        val a = Node("25565", slow, redisSettings = enabledRedis, coordinator = DatabaseSessionCoordinator(db, "25565", slow.lease)).also { node ->
            node.lifecycle.cacheFactory = { flaky }
            node.registry.register(wallet)
            node.lifecycle.attach(wallet)
        }
        val started = System.currentTimeMillis()
        recoversAt.set(started + 300)

        a.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil(3000) { loadedListener.loaded.contains(uuid) }

        assertTrue(System.currentTimeMillis() - started >= 300)
        assertTrue(reads.get() > 2)
        assertTrue(player.isOnline)
        assertEquals(Wallet(100), wallet[uuid])
        assertNotNull(a.sessions.get(uuid))
    }

    @Test
    fun `cache is not attached when redis is disabled`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        val a = cachedNode(memory, wallet, disabledRedis)

        joined(a)
        wallet.update(uuid) { it.coins = 42 }
        tickFor(100)

        assertFalse(wallet.cacheEnabled)
        assertEquals(1, wallet.loads.get())
        assertTrue(memory.values.isEmpty())
    }

    @Test
    fun `offline write saves, bumps the version and releases ownership`() {
        val a = Node("25565")
        joined(a)
        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil { a.sent.isNotEmpty() }
        val before = version()

        val result = runBlocking { a.repo.writeOffline(uuid, Points(7)) }

        assertEquals(OfflineWriteResult.Written, result)
        assertEquals(7, storedPoints())
        assertEquals(before + 1, version())
        assertNull(owner())
        assertNull(a.repo[uuid])
    }

    @Test
    fun `offline write is held while the player is online`() {
        val a = Node("25565")
        joined(a)

        val result = runBlocking { a.repo.writeOffline(uuid, Points(7)) }

        assertEquals(OfflineWriteResult.Held("25565"), result)
        assertNull(storedPoints())
    }

    @Test
    fun `offline write is held by the owner of a live lease on another server`() {
        val a = Node("25565")
        insertSession("25566", Instant.now().plusSeconds(60))

        val result = runBlocking { a.repo.writeOffline(uuid, Points(7)) }

        assertEquals(OfflineWriteResult.Held("25566"), result)
        assertNull(storedPoints())
    }

    @Test
    fun `offline write refreshes the cached copy with the data ttl`() {
        val memory = InMemoryPlayerDataCache()
        val wallet = WalletRepository()
        cachedNode(memory, wallet)
        memory.values[walletKey(wallet)] = """{"coins":7}""".toByteArray()

        val result = runBlocking { wallet.writeOffline(uuid, Wallet(9)) }

        assertEquals(OfflineWriteResult.Written, result)
        assertEquals("""{"coins":9}""", memory.text(walletKey(wallet)))
        assertEquals(enabledRedis.dataTtl, memory.ttls[walletKey(wallet)])
    }
}
