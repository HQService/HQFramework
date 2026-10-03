package kr.hqservice.framework.database.repository.player.session

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

object PlayerDataBackendMarker {
    private const val MARKER_ID = 1
    const val MANUAL_SWITCH_SQL = "UPDATE hqframework_player_data_backend SET backend = '%s' WHERE id = 1"

    fun ensure(database: Database, backend: String): Boolean {
        val stored = try {
            transaction(database) {
                SchemaUtils.create(PlayerDataBackendTable)
                storedBackend() ?: insertBackend(backend)
            }
        } catch (e: ExposedSQLException) {
            runCatching { transaction(database) { storedBackend() } }.getOrNull() ?: throw e
        }
        if (stored == backend) return false
        if (stored == "database" && backend == "redis") {
            val live = transaction(database) {
                SchemaUtils.create(PlayerSessionTable)
                liveSessionCount()
            }
            check(live == 0L) {
                "player-data.backend is 'redis' but $live player session(s) are still held through the 'database' backend; " +
                    "another server is probably still running with backend 'database'. Stop every server and start again, " +
                    "or if you are sure no server uses the old backend run: ${MANUAL_SWITCH_SQL.format("redis")}"
            }
            transaction(database) {
                PlayerDataBackendTable.update({ PlayerDataBackendTable.id eq MARKER_ID }) { it[PlayerDataBackendTable.backend] = backend }
            }
            return true
        }
        throw IllegalStateException(
            "player-data.backend is '$backend' but the shared database was initialised with '$stored'; all servers must use the same backend. " +
                "Switching from '$stored' to '$backend' is manual: stop every server, make sure no player session is still owned through '$stored', " +
                "then run: ${MANUAL_SWITCH_SQL.format(backend)}"
        )
    }

    fun stored(): String? = storedBackend()

    private fun liveSessionCount(): Long =
        PlayerSessionTable.selectAll()
            .where { PlayerSessionTable.owner.isNotNull() and (PlayerSessionTable.leaseUntil greater CurrentTimestamp<Instant>()) }
            .count()

    private fun insertBackend(backend: String): String {
        PlayerDataBackendTable.insert {
            it[id] = MARKER_ID
            it[PlayerDataBackendTable.backend] = backend
        }
        return backend
    }

    private fun storedBackend(): String? =
        PlayerDataBackendTable.selectAll()
            .where { PlayerDataBackendTable.id eq MARKER_ID }
            .singleOrNull()
            ?.get(PlayerDataBackendTable.backend)
}
