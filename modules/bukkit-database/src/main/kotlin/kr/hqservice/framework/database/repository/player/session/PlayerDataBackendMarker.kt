package kr.hqservice.framework.database.repository.player.session

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object PlayerDataBackendMarker {
    private const val MARKER_ID = 1

    fun ensure(database: Database, backend: String) {
        val stored = transaction(database) {
            SchemaUtils.create(PlayerDataBackendTable)
            storedBackend() ?: try {
                PlayerDataBackendTable.insert {
                    it[id] = MARKER_ID
                    it[PlayerDataBackendTable.backend] = backend
                }
                backend
            } catch (e: ExposedSQLException) {
                storedBackend() ?: throw e
            }
        }
        check(stored == backend) {
            "player-data.backend is '$backend' but the shared database was initialised with '$stored'; all servers must use the same backend"
        }
    }

    private fun storedBackend(): String? =
        PlayerDataBackendTable.selectAll()
            .where { PlayerDataBackendTable.id eq MARKER_ID }
            .singleOrNull()
            ?.get(PlayerDataBackendTable.backend)
}
