package kr.hqservice.framework.database.datasource

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SQLiteDataSourceTest {
    @Test
    fun `connections use WAL journal and a busy timeout`(@TempDir dir: File) {
        SQLiteDataSource(File(dir, "test.db").path).use { dataSource ->
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("PRAGMA journal_mode").use {
                        it.next()
                        assertEquals("wal", it.getString(1))
                    }
                    statement.executeQuery("PRAGMA busy_timeout").use {
                        it.next()
                        assertEquals(5000, it.getInt(1))
                    }
                }
            }
            assertEquals(1, dataSource.maximumPoolSize)
        }
    }
}
