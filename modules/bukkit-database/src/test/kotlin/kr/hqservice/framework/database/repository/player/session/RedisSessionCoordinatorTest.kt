package kr.hqservice.framework.database.repository.player.session

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.database.redis.InMemoryPubSubTransport
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.repository.player.session.redis.InMemorySessionStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.UUID

class RedisSessionCoordinatorTest {
    private val store = InMemorySessionStore()
    private val transport = InMemoryPubSubTransport()
    private val settings = RedisSettings("", "hq", Duration.ofSeconds(3600))
    private val lease = Duration.ofSeconds(30)
    private val a = RedisSessionCoordinator(store, settings, lease, "25565", transport)
    private val b = RedisSessionCoordinator(store, settings, lease, "25566", transport)
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
    fun `same owner can acquire again and keeps the version`() = runBlocking {
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
        a.commit(playerId, 0)
        assertEquals(AcquireResult.Acquired(1), a.acquire(playerId))
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
}
