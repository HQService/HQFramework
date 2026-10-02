package kr.hqservice.framework.database.repository.player

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.cache.InMemoryPlayerDataCache
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.Duration
import java.util.UUID

class CachedPlayerRepositoryTest {
    @Serializable
    data class Wallet(var coins: Int)

    class WalletRepository(
        serializer: KSerializer<Wallet> = Wallet.serializer(),
        private val offline: Wallet? = null,
    ) : CachedPlayerRepository<Wallet>(serializer) {
        override suspend fun load(player: Player): Wallet = Wallet(0)

        override suspend fun save(player: Player, value: Wallet) {}

        override suspend fun loadOffline(uuid: UUID): Wallet? = offline
    }

    object ForbiddenSerializer : KSerializer<Wallet> {
        override val descriptor: SerialDescriptor = Wallet.serializer().descriptor

        override fun serialize(encoder: Encoder, value: Wallet) = fail("serializer must not be used without redis")

        override fun deserialize(decoder: Decoder): Wallet = fail("serializer must not be used without redis")
    }

    private val uuid = UUID.randomUUID()
    private val settings = RedisSettings("redis://localhost", "hq", Duration.ofSeconds(90))
    private val cache = InMemoryPlayerDataCache()
    private val queued = mutableListOf<suspend () -> Unit>()

    @BeforeEach
    fun setUp() {
        Database.connect("jdbc:h2:mem:cached_repository;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }

    private fun <R : CachedPlayerRepository<*>> R.enabled(): R = apply {
        cache = this@CachedPlayerRepositoryTest.cache
        cacheSettings = settings
        cacheWriter = { _, block -> runBlocking { block() } }
    }

    private fun key(repository: CachedPlayerRepository<*>): String = "hq:data:${repository.cacheName}:$uuid"

    @Test
    fun `update writes the serialized value under the data key`() {
        val repository = WalletRepository().enabled()
        repository.put(uuid, Wallet(0))

        repository.update(uuid) { it.coins = 5 }

        assertEquals("hq:data:${WalletRepository::class.java.name}:$uuid", repository.cacheKey(uuid))
        assertEquals("""{"coins":5}""", cache.text(key(repository)))
    }

    @Test
    fun `set writes the new value`() {
        val repository = WalletRepository().enabled()
        repository.put(uuid, Wallet(0))

        repository[uuid] = Wallet(9)

        assertEquals("""{"coins":9}""", cache.text(key(repository)))
    }

    @Test
    fun `mutation of a player that is not loaded writes nothing`() {
        val repository = WalletRepository().enabled()

        repository.update(uuid) { it.coins = 5 }
        repository[uuid] = Wallet(9)

        assertTrue(cache.values.isEmpty())
    }

    @Test
    fun `queued writes are coalesced and write the latest value`() = runBlocking {
        val repository = WalletRepository().enabled()
        repository.cacheWriter = { _, block -> queued += block }
        repository.put(uuid, Wallet(0))

        repository.update(uuid) { it.coins = 1 }
        repository.update(uuid) { it.coins = 2 }
        assertEquals(1, queued.size)
        queued.removeAt(0)()
        repository.update(uuid) { it.coins = 3 }
        queued.removeAt(0)()

        assertEquals(2, cache.writes.get())
        assertEquals("""{"coins":3}""", cache.text(key(repository)))
    }

    @Test
    fun `readCached decodes the cached value`() = runBlocking {
        val repository = WalletRepository().enabled()
        cache.values[key(repository)] = """{"coins":7}""".toByteArray()

        assertEquals(Wallet(7), repository.readCached(uuid))
    }

    @Test
    fun `malformed cached value is ignored`() = runBlocking {
        val repository = WalletRepository().enabled()
        cache.values[key(repository)] = "not json".toByteArray()

        assertNull(repository.readCached(uuid))
    }

    @Test
    fun `peek prefers the cached value and falls back to loadOffline`() = runBlocking {
        val repository = WalletRepository(offline = Wallet(3)).enabled()

        assertEquals(Wallet(3), repository.peek(uuid))

        cache.values[key(repository)] = """{"coins":7}""".toByteArray()
        assertEquals(Wallet(7), repository.peek(uuid))
        assertFalse(repository.contains(uuid))
    }

    @Test
    fun `persisting an offline player sets the data ttl`() = runBlocking {
        val repository = WalletRepository().enabled()
        repository.put(uuid, Wallet(0))
        repository.update(uuid) { it.coins = 5 }

        repository.afterPersisted(uuid, offline = false)
        assertNull(cache.ttls[key(repository)])

        repository.afterPersisted(uuid, offline = true)
        assertEquals(Duration.ofSeconds(90), cache.ttls[key(repository)])
    }

    @Test
    fun `without redis it behaves like a plain repository and never serializes`() = runBlocking {
        val repository = WalletRepository(ForbiddenSerializer, offline = Wallet(3))
        repository.cacheWriter = { _, block -> runBlocking { block() } }
        repository.put(uuid, Wallet(0))

        repository.update(uuid) { it.coins = 5 }
        repository[uuid] = Wallet(6)
        repository.writeCached(uuid, Wallet(6))
        repository.afterPersisted(uuid, offline = true)

        assertFalse(repository.cacheEnabled)
        assertEquals(Wallet(6), repository[uuid])
        assertTrue(repository.isDirty(uuid))
        assertNull(repository.readCached(uuid))
        assertEquals(Wallet(3), repository.peek(uuid))
        assertTrue(cache.values.isEmpty())
    }
}
