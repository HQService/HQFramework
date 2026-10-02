package kr.hqservice.framework.database.repository.player.session

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class PlayerDataBackendMarkerTest {
    private val db = Database.connect("jdbc:h2:mem:backend_marker;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    @BeforeEach
    fun setUp() {
        transaction(db) { SchemaUtils.drop(PlayerDataBackendTable) }
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
    fun `different backend fails`() {
        PlayerDataBackendMarker.ensure(db, "database")
        val error = assertThrows<IllegalStateException> { PlayerDataBackendMarker.ensure(db, "redis") }
        assertTrue(error.message!!.contains("'database'"), error.message)
        assertEquals("database", stored())
    }
}
