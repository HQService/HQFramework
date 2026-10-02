package kr.hqservice.framework.database.repository.player.session

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class DatabaseSessionCoordinatorTest {
    private val db = Database.connect("jdbc:h2:mem:session;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    private val a = DatabaseSessionCoordinator(db, "25565", Duration.ofSeconds(30))
    private val b = DatabaseSessionCoordinator(db, "25566", Duration.ofSeconds(30))
    private val playerId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        transaction(db) {
            SchemaUtils.drop(PlayerSessionTable)
            SchemaUtils.create(PlayerSessionTable)
        }
    }

    private fun setLeaseUntil(value: Instant) {
        transaction(db) {
            PlayerSessionTable.update({ PlayerSessionTable.uuid eq playerId }) { it[leaseUntil] = value }
        }
    }

    private fun currentLeaseUntil(): Instant? = transaction(db) {
        PlayerSessionTable.selectAll()
            .where { PlayerSessionTable.uuid eq playerId }
            .single()[PlayerSessionTable.leaseUntil]
    }

    @Test
    fun `first acquire inserts and other server is held`() = runBlocking {
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
    }

    @Test
    fun `release lets other server acquire and only owner can release`() = runBlocking {
        a.acquire(playerId)
        assertTrue(a.release(playerId))
        assertEquals(AcquireResult.Acquired(0), b.acquire(playerId))
        assertFalse(a.release(playerId))
    }

    @Test
    fun `expired lease is taken over`() = runBlocking {
        a.acquire(playerId)
        setLeaseUntil(Instant.now().minusSeconds(60))
        assertEquals(AcquireResult.Acquired(0), b.acquire(playerId))
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
    fun `renew extends lease only for owner`() = runBlocking {
        a.acquire(playerId)
        val past = Instant.now().minusSeconds(10)
        setLeaseUntil(past)
        b.renew(listOf(playerId))
        assertEquals(past.epochSecond, currentLeaseUntil()!!.epochSecond)
        a.renew(listOf(playerId))
        assertTrue(currentLeaseUntil()!!.isAfter(Instant.now()))
    }

    @Test
    fun `commit joins an existing transaction`() = runBlocking {
        a.acquire(playerId)
        val result = newSuspendedTransaction(db = db) { a.commit(playerId, 0) }
        assertEquals(1L, result)
    }

    @Test
    fun `commit inside a transaction of another database uses its own database`() = runBlocking {
        val other = Database.connect("jdbc:h2:mem:session_other;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        a.acquire(playerId)
        val result = newSuspendedTransaction(db = other) { a.commit(playerId, 0) }
        assertEquals(1L, result)
    }

    @Test
    fun `same owner can acquire again`() = runBlocking {
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
    }
}
