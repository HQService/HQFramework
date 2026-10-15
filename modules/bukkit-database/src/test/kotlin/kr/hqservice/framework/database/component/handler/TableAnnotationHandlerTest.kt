package kr.hqservice.framework.database.component.handler

import kr.hqservice.framework.database.component.Table
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.reflect.full.findAnnotation

class TableAnnotationHandlerTest {
    object V1 : org.jetbrains.exposed.sql.Table("migrate_t") {
        val id = integer("id")
    }

    @Table(withLogs = false)
    object V2 : org.jetbrains.exposed.sql.Table("migrate_t") {
        val id = integer("id")
        val name = varchar("name", 16)
    }

    @Test
    fun `missing columns are added to an existing table`() {
        val db = Database.connect("jdbc:h2:mem:migrate;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) { SchemaUtils.create(V1) }

        TableAnnotationHandler(db, Logger.getAnonymousLogger()).setup(V2, V2::class.findAnnotation<Table>()!!)

        assertDoesNotThrow {
            transaction(db) {
                V2.insert {
                    it[id] = 1
                    it[name] = "a"
                }
            }
        }
    }

    @Test
    fun `failing schema update is logged instead of thrown`() {
        val db = Database.connect("jdbc:h2:mem:migrate_fail;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.create(V1)
            V1.insert { it[id] = 1 }
        }

        assertDoesNotThrow {
            TableAnnotationHandler(db, Logger.getAnonymousLogger()).setup(V2, V2::class.findAnnotation<Table>()!!)
        }
    }
}
