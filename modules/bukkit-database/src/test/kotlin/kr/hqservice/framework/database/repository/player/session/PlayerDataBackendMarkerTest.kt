package kr.hqservice.framework.database.repository.player.session

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class PlayerDataBackendMarkerTest {
    private val db = Database.connect("jdbc:h2:mem:backend_marker;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    @BeforeEach
    fun setUp() {
        transaction(db) {
            SchemaUtils.drop(PlayerDataBackendTable, PlayerSessionTable)
            SchemaUtils.create(PlayerSessionTable)
        }
    }

    private fun insertSession(owner: String?, leaseUntil: Instant) = transaction(db) {
        PlayerSessionTable.insert {
            it[uuid] = UUID.randomUUID()
            it[PlayerSessionTable.owner] = owner
            it[version] = 1
            it[PlayerSessionTable.leaseUntil] = leaseUntil
        }
    }

    private fun stored(): String = transaction(db) {
        PlayerDataBackendTable.selectAll().single()[PlayerDataBackendTable.backend]
    }

    @Test
    fun `first initialisation stores the backend`() {
        PlayerDataBackendMarker.ensure(db, "database")
        assertEquals("database", stored())
    }

    @Test
    fun `same backend passes`() {
        PlayerDataBackendMarker.ensure(db, "redis")
        assertDoesNotThrow { PlayerDataBackendMarker.ensure(db, "redis") }
        assertEquals("redis", stored())
    }

    @Test
    fun `database to redis switches automatically when no session is held`() {
        PlayerDataBackendMarker.ensure(db, "database")
        insertSession(null, Instant.now().plusSeconds(60))
        insertSession("25565", Instant.now().minusSeconds(60))

        assertTrue(PlayerDataBackendMarker.ensure(db, "redis"))
        assertEquals("redis", stored())
        assertFalse(PlayerDataBackendMarker.ensure(db, "redis"))
    }

    @Test
    fun `database to redis is refused while a session is still held`() {
        PlayerDataBackendMarker.ensure(db, "database")
        insertSession("25565", Instant.now().plusSeconds(60))

        val error = assertThrows<IllegalStateException> { PlayerDataBackendMarker.ensure(db, "redis") }
        assertTrue(error.message!!.contains("1 player session(s)"), error.message)
        assertTrue(error.message!!.contains("UPDATE hqframework_player_data_backend"), error.message)
        assertEquals("database", stored())
    }

    @Test
    fun `redis to database stays manual`() {
        PlayerDataBackendMarker.ensure(db, "redis")
        val error = assertThrows<IllegalStateException> { PlayerDataBackendMarker.ensure(db, "database") }
        assertTrue(error.message!!.contains("'redis'"), error.message)
        assertTrue(error.message!!.contains("manual"), error.message)
        assertEquals("redis", stored())
    }
}
