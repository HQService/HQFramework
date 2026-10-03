package kr.hqservice.framework.database.repository.player.lifecycle

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.TestPlugin
import kr.hqservice.framework.database.redis.LettucePubSubTransport
import kr.hqservice.framework.database.redis.RedisProvider
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.CachedPlayerRepository
import kr.hqservice.framework.database.repository.player.PlayerDataSettings
import kr.hqservice.framework.database.repository.player.registry.impl.PlayerRepositoryRegistryImpl
import kr.hqservice.framework.database.repository.player.session.RedisSessionCoordinator
import kr.hqservice.framework.database.repository.player.session.redis.LettuceSessionStore
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Packet
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class RedisEndToEndTest {
    @Serializable
    data class PointData(var n: Int)

    object PointTable : Table("redis_e2e_point") {
        val uuid = uuid("uuid")
        val n = integer("n")
        override val primaryKey = PrimaryKey(uuid)
    }

    class PointRepository : CachedPlayerRepository<PointData>(PointData.serializer()) {
        val loads = AtomicInteger()

        override suspend fun load(player: Player): PointData {
            loads.incrementAndGet()
            val row = PointTable.selectAll().where { PointTable.uuid eq player.uniqueId }.singleOrNull()
            return PointData(row?.get(PointTable.n) ?: 0)
        }

        override suspend fun save(player: Player, value: PointData) {
            PointTable.upsert {
                it[uuid] = player.uniqueId
                it[n] = value.n
            }
        }
    }

    inner class Node(serverId: String, settings: PlayerDataSettings = this@RedisEndToEndTest.settings()) {
        val provider = RedisProvider(redis, logger).also(providers::add)
        val transport = LettucePubSubTransport(provider, logger).also(transports::add)
        val coordinator = RedisSessionCoordinator(LettuceSessionStore(provider), redis, settings.lease, serverId, transport)
        val repo = PointRepository()
        val registry = PlayerRepositoryRegistryImpl().also { it.register(repo) }
        val sessions = PlayerSessionRegistry()
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
            LoadingPlayers(),
            server.pluginManager,
            packetSender,
            mockk<HQNettyService>().also { every { it.isEnable() } returns true },
            logger,
            redis,
            provider,
            Json,
        ).also(lifecycles::add)
    }

    private fun settings(joinTimeout: Duration = Duration.ofSeconds(5), retryInterval: Duration = Duration.ofMillis(50)) =
        PlayerDataSettings(
            "redis",
            Duration.ofSeconds(60),
            Duration.ofSeconds(30),
            joinTimeout,
            retryInterval,
            Duration.ofSeconds(60),
            Duration.ofSeconds(60),
        )

    private val logged: MutableList<String> = Collections.synchronizedList(mutableListOf())
    private val logger = Logger.getLogger("RedisEndToEndTest-${UUID.randomUUID()}")
    private val providers = mutableListOf<RedisProvider>()
    private val transports = mutableListOf<LettucePubSubTransport>()
    private val lifecycles = mutableListOf<PlayerDataLifecycle>()
    private lateinit var redis: RedisSettings
    private lateinit var inspector: RedisProvider
    private lateinit var server: ServerMock
    private lateinit var plugin: TestPlugin
    private lateinit var db: Database
    private lateinit var player: PlayerMock
    private lateinit var uuid: UUID

    @BeforeEach
    fun setUp() {
        val uri = System.getenv("HQ_TEST_REDIS_URI")
        Assumptions.assumeTrue(uri != null)
        redis = RedisSettings(uri!!, "hqtest-${UUID.randomUUID()}", Duration.ofSeconds(60))
        inspector = RedisProvider(redis, logger)
        server = MockBukkit.mock()
        logger.addHandler(object : Handler() {
            override fun publish(record: LogRecord) {
                logged += record.message
            }

            override fun flush() {}

            override fun close() {}
        })
        plugin = TestPlugin.load(server)
        db = Database.connect("jdbc:h2:mem:redis_e2e;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.drop(PointTable)
            SchemaUtils.create(PointTable)
        }
        player = server.addPlayer()
        uuid = player.uniqueId
    }

    @AfterEach
    fun tearDown() {
        if (!::inspector.isInitialized) return
        lifecycles.forEach { it.shutdown() }
        transports.forEach { it.close() }
        providers.forEach { it.close() }
        MockBukkit.unmock()
        val commands = inspector.connection().sync()
        val created = commands.keys("${redis.keyPrefix}:*")
        if (created.isNotEmpty()) commands.del(*created.toTypedArray())
        inspector.close()
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

    private fun joined(node: Node) {
        node.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        awaitUntil { node.sessions.get(uuid) != null }
    }

    private fun sessionKey(): String = redis.key("session", uuid.toString())

    private fun dataKey(repo: PointRepository): String = redis.key("data", repo.cacheName, uuid.toString())

    private fun session(field: String): String? = inspector.connection().sync().hget(sessionKey(), field)?.decodeToString()

    private fun cached(repo: PointRepository): String? = inspector.connection().sync().get(dataKey(repo))?.decodeToString()

    private fun storedPoints(): Int? = transaction(db) {
        PointTable.selectAll().where { PointTable.uuid eq uuid }.singleOrNull()?.get(PointTable.n)
    }

    @Test
    fun `join takes the redis session and updates are cached as json`() {
        val a = Node("25565")

        joined(a)
        a.repo.update(uuid) { it.n = 7 }

        assertEquals("${redis.keyPrefix}:data:${a.repo.cacheName}:$uuid", dataKey(a.repo))
        awaitUntil { cached(a.repo) == """{"n":7}""" }
        assertEquals("25565", session("owner"))
        assertEquals("0", session("version"))
        assertEquals(-1L, inspector.connection().sync().pttl(dataKey(a.repo)))
    }

    @Test
    fun `switching server acquires through the release notification and loads from the cache`() {
        val a = Node("25565")
        val b = Node("25566", settings(joinTimeout = Duration.ofSeconds(10), retryInterval = Duration.ofSeconds(5)))
        joined(a)
        a.repo.update(uuid) { it.n = 7 }
        awaitUntil { cached(a.repo) == """{"n":7}""" }

        b.lifecycle.onJoin(PlayerJoinEvent(player, "join"))
        tickFor(300)
        assertFalse(b.repo.contains(uuid))
        assertEquals("25565", session("owner"))

        val quitAt = System.currentTimeMillis()
        a.lifecycle.onQuit(PlayerQuitEvent(player, "quit"))
        awaitUntil(5000) { b.sessions.get(uuid) != null }
        val acquiredAfter = System.currentTimeMillis() - quitAt

        assertTrue(acquiredAfter < 500, "acquired ${acquiredAfter}ms after quit")
        assertEquals("25566", session("owner"))
        assertEquals("1", session("version"))
        assertEquals(PointData(7), b.repo[uuid])
        assertEquals(0, b.repo.loads.get())
        assertEquals(7, storedPoints())
        assertEquals(-1L, inspector.connection().sync().pttl(dataKey(b.repo)))
    }

    private fun crashTakeover(): Pair<Node, Node> {
        val a = Node("25565")
        val b = Node("25566")
        joined(a)
        a.repo.update(uuid) { it.n = 5 }
        assertTrue(runBlocking { a.repo.flush(uuid) })
        assertEquals("1", session("version"))
        awaitUntil { cached(a.repo) == """{"n":5}""" }

        inspector.connection().sync().hset(sessionKey(), "lease_until", "0".toByteArray())
        joined(b)
        return a to b
    }

    @Test
    fun `expired lease of a crashed owner is taken over with the persisted version from the cache`() {
        val (_, b) = crashTakeover()

        assertEquals("25566", session("owner"))
        assertEquals(1L, b.sessions.get(uuid)!!.version)
        assertEquals(PointData(5), b.repo[uuid])
        assertEquals(0, b.repo.loads.get())
    }

    @Test
    fun `writes of the previous owner after a takeover are fenced and its flush loses ownership`() {
        val (a, b) = crashTakeover()

        a.repo.update(uuid) { it.n = 99 }
        awaitUntil { logged.any { it.contains("no longer owns") } }

        assertEquals("""{"n":5}""", cached(a.repo))

        assertFalse(runBlocking { a.repo.flush(uuid) })
        awaitUntil { !player.isOnline }

        assertNull(a.sessions.get(uuid))
        assertFalse(a.repo.contains(uuid))
        assertTrue(logged.any { it.contains("ownership lost") })
        assertEquals(5, storedPoints())
        assertEquals("""{"n":5}""", cached(b.repo))
        assertEquals("25566", session("owner"))
        assertEquals("1", session("version"))
    }
}
