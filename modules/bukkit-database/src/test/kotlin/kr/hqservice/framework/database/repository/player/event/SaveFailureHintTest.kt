package kr.hqservice.framework.database.repository.player.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.sql.SQLTransientConnectionException

class SaveFailureHintTest {
    @Test
    fun `data truncation is classified as a column size problem even when wrapped`() {
        val cause = IllegalStateException("flush failed", SQLException("Data truncation: Data too long for column 'inventory' at row 1"))
        assertEquals(SaveFailureHint.COLUMN_TOO_SMALL, SaveFailureHint.of(cause))
    }

    @Test
    fun `connection problems are classified by exception type or message`() {
        assertEquals(SaveFailureHint.CONNECTION, SaveFailureHint.of(SQLTransientConnectionException("hqframework - Connection is not available, request timed out after 30000ms")))
        assertEquals(SaveFailureHint.CONNECTION, SaveFailureHint.of(SQLException("Communications link failure")))
        assertEquals(SaveFailureHint.CONNECTION, SaveFailureHint.of(SQLException("Connection is closed")))
    }

    @Test
    fun `missing tables and unknown errors`() {
        assertEquals(SaveFailureHint.MISSING_SCHEMA, SaveFailureHint.of(SQLException("Table 'hq.hqsynchronizer_inventory' doesn't exist")))
        assertEquals(SaveFailureHint.UNKNOWN, SaveFailureHint.of(IllegalStateException("db down")))
    }
}
