package kr.hqservice.framework.database.repository.player.session

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Duration
import java.util.UUID

class SqliteSessionCoordinatorTest {
    @TempDir
    lateinit var dir: File

    private lateinit var db: Database
    private lateinit var a: DatabaseSessionCoordinator
    private lateinit var b: DatabaseSessionCoordinator
    private val playerId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        db = Database.connect("jdbc:sqlite:${File(dir, "session.db").path}", driver = "org.sqlite.JDBC")
        transaction(db) { SchemaUtils.create(PlayerSessionTable) }
        a = DatabaseSessionCoordinator(db, "25565", Duration.ofSeconds(30))
        b = DatabaseSessionCoordinator(db, "25566", Duration.ofSeconds(30))
    }

    @Test
    fun `first acquire inserts and other server is held`() = runBlocking {
        assertEquals(AcquireResult.Acquired(0), a.acquire(playerId))
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
    }

    @Test
    fun `release lets other server acquire`() = runBlocking {
        a.acquire(playerId)
        assertTrue(a.release(playerId))
        assertEquals(AcquireResult.Acquired(0), b.acquire(playerId))
        assertFalse(a.release(playerId))
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
    fun `expired lease is taken over`() = runBlocking {
        a.acquire(playerId)
        assertEquals(AcquireResult.Held("25565"), b.acquire(playerId))
        transaction(db) {
            exec("UPDATE hqframework_player_session SET lease_until = DATETIME('now', '-60 seconds') WHERE owner = '25565'")
        }
        assertEquals(AcquireResult.Acquired(0), b.acquire(playerId))
    }
}
