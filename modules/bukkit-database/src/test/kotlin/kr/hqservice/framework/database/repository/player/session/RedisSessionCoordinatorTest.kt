package kr.hqservice.framework.database.repository.player.session

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.database.redis.InMemoryPubSubTransport
import kr.hqservice.framework.database.redis.PubSubTransport
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.session.redis.InMemorySessionStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

class RedisSessionCoordinatorTest {
    private val store = InMemorySessionStore()
    private val transport = InMemoryPubSubTransport()
    private val settings = RedisSettings("", "hq", Duration.ofSeconds(3600))
    private val lease = Duration.ofSeconds(30)
    private val logger = mockk<Logger>(relaxed = true)
    private val a = RedisSessionCoordinator(store, settings, lease, "25565", transport, logger)
    private val b = RedisSessionCoordinator(store, settings, lease, "25566", transport, logger)
    private val playerId = UUID.randomUUID()
    private val key = "hq:session:$playerId"

    @Test
    fun `first acquire creates the session and other server is held`() = runBlocking {
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
        assertEquals("25565", store.entry(key)!!.owner)
    }

    @Test
    fun `release lets other server acquire and only owner can release`() = runBlocking {
        a.acquire(playerId)
        assertTrue(a.release(playerId))
        assertEquals(AcquireResult.Acquired(0), b.acquire(playerId))
        assertFalse(a.release(playerId))
    }

    @Test
    fun `release keeps the committed version for the next owner`() = runBlocking {
        a.acquire(playerId)
        a.commit(playerId, 0)

        assertTrue(a.release(playerId))

        assertNull(store.entry(key)!!.owner)
        assertNull(store.entry(key)!!.leaseUntil)
        assertEquals(AcquireResult.Acquired(1), b.acquire(playerId))
    }

    @Test
    fun `lease is held until it has passed`() = runBlocking {
        a.acquire(playerId)
        store.now += lease.toMillis()
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
    }

    @Test
    fun `expired lease is taken over with the committed version`() = runBlocking {
        a.acquire(playerId)
        a.commit(playerId, 0)
        store.now += lease.toMillis() + 1
        assertEquals(AcquireResult.Acquired(1), b.acquire(playerId))
        assertEquals("25566", store.entry(key)!!.owner)
    }

    @Test
    fun `commit of the owner does not check the lease expiry`() = runBlocking {
        a.acquire(playerId)
        store.now += lease.toMillis() + 1
        assertEquals(1L, a.commit(playerId, 0))
    }

    @Test
    fun `commit rejects stale version and non owner`() = runBlocking {
        a.acquire(playerId)
        assertEquals(1L, a.commit(playerId, 0))
        assertNull(a.commit(playerId, 0))
        assertNull(b.commit(playerId, 1))
        assertEquals(2L, a.commit(playerId, 1))
    }

    @Test
    fun `commit extends the lease`() = runBlocking {
        a.acquire(playerId)
        store.now += 20_000
        a.commit(playerId, 0)
        store.now += 20_000
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
    }

    @Test
    fun `renew extends lease only for owner`() = runBlocking {
        a.acquire(playerId)
        store.now += 20_000
        b.renew(listOf(playerId))
        assertEquals(30_000L, store.entry(key)!!.leaseUntil)
        a.renew(listOf(playerId))
        assertEquals(50_000L, store.entry(key)!!.leaseUntil)
    }

    @Test
    fun `renew reclaims a session that vanished from redis`() = runBlocking {
        a.acquire(playerId)
        a.commit(playerId, 0)
        store.wipe(key)

        a.renew(listOf(playerId))

        assertEquals("25565", store.owner(key))
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
    }

    @Test
    fun `renew does not reclaim a released session`() = runBlocking {
        a.acquire(playerId)
        assertTrue(a.release(playerId))

        a.renew(listOf(playerId))

        assertNull(store.owner(key))
        assertEquals(AcquireResult.Acquired(0), b.acquire(playerId))
    }

    @Test
    fun `commit reclaims a vanished session and continues the version`() = runBlocking {
        a.acquire(playerId)
        a.commit(playerId, 0)
        store.wipe(key)

        assertEquals(2L, a.commit(playerId, 1))

        assertEquals("25565", store.owner(key))
        assertEquals(2L, a.ownedVersion(playerId))
        assertNull(b.commit(playerId, 2))
    }

    @Test
    fun `commit after a renew reclaim restores the version of the owner`() = runBlocking {
        a.acquire(playerId)
        a.commit(playerId, 0)
        store.wipe(key)
        a.renew(listOf(playerId))

        assertNull(a.ownedVersion(playerId))
        assertEquals(2L, a.commit(playerId, 1))
        assertEquals(2L, a.ownedVersion(playerId))
    }

    @Test
    fun `verify reclaims a vanished session with the version of the owner`() = runBlocking {
        a.acquire(playerId)
        a.commit(playerId, 0)
        store.wipe(key)

        assertTrue(a.verify(playerId, 1))

        assertEquals("25565", store.owner(key))
        assertEquals(1L, a.ownedVersion(playerId))
        assertFalse(b.verify(playerId, 1))
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
    }

    @Test
    fun `same owner can acquire again and keeps the version`() = runBlocking {
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
        a.commit(playerId, 0)
        assertEquals(AcquireResult.Acquired(1), a.acquire(playerId))
    }

    @Test
    fun `verify checks the owner and the version`() = runBlocking {
        a.acquire(playerId)
        assertTrue(a.verify(playerId, 0))
        assertFalse(a.verify(playerId, 1))
        assertFalse(b.verify(playerId, 0))
        a.commit(playerId, 0)
        assertTrue(a.verify(playerId, 1))
        a.release(playerId)
        assertFalse(a.verify(playerId, 1))
    }

    @Test
    fun `release notifies released listeners`() = runBlocking {
        val released = mutableListOf<UUID>()
        b.onReleased { released += it }
        a.acquire(playerId)

        a.release(playerId)

        assertEquals(listOf(playerId), released)
        assertEquals(setOf("hq:session-released"), transport.listeners.keys)
    }

    @Test
    fun `failed release does not notify`() = runBlocking {
        val released = mutableListOf<UUID>()
        b.onReleased { released += it }
        a.acquire(playerId)

        b.release(playerId)

        assertTrue(released.isEmpty())
    }

    @Test
    fun `closed released listener is not called`() = runBlocking {
        val released = mutableListOf<UUID>()
        val subscription = b.onReleased { released += it }
        a.acquire(playerId)

        subscription.close()
        a.release(playerId)

        assertTrue(released.isEmpty())
    }

    @Test
    fun `malformed released payload is ignored`() {
        val released = mutableListOf<UUID>()
        b.onReleased { released += it }

        transport.publish("hq:session-released", "not a uuid".toByteArray())

        assertTrue(released.isEmpty())
    }

    @Test
    fun `owned version is the current version only for the owner`() = runBlocking {
        assertNull(a.ownedVersion(playerId))
        a.acquire(playerId)
        a.commit(playerId, 0)
        assertEquals(1L, a.ownedVersion(playerId))
        assertNull(b.ownedVersion(playerId))
        a.release(playerId)
        assertNull(a.ownedVersion(playerId))
    }

    @Test
    fun `release succeeds even when publishing the release notification fails`() = runBlocking {
        val failing = object : PubSubTransport {
            override fun publish(channel: String, payload: ByteArray) = throw IllegalStateException("redis down")

            override fun subscribe(channel: String, listener: (ByteArray) -> Unit): AutoCloseable = AutoCloseable { }
        }
        val coordinator = RedisSessionCoordinator(store, settings, lease, "25565", failing, logger)
        coordinator.acquire(playerId)

        assertTrue(coordinator.release(playerId))

        assertNull(store.entry(key)!!.owner)
        verify { logger.log(Level.WARNING, match<String> { it.contains("release notification") }, any<Throwable>()) }
    }
}
