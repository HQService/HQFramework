package kr.hqservice.framework.database.repository.player.session

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.vendors.H2Dialect
import org.jetbrains.exposed.sql.vendors.MysqlDialect
import org.jetbrains.exposed.sql.vendors.SQLiteDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.time.Duration
import java.time.Instant
import java.util.UUID

class DatabaseSessionCoordinator(
    private val database: Database,
    override val serverId: String,
    private val lease: Duration
) : SessionCoordinator {
    override val commitsInsideTransaction: Boolean = true

    override suspend fun acquire(uuid: UUID): AcquireResult = inTransaction {
        tryAcquire(uuid) ?: run {
            val current = findSession(uuid)
            if (current != null) {
                AcquireResult.Held(current[PlayerSessionTable.owner] ?: "")
            } else {
                insertSession(uuid)
                tryAcquire(uuid) ?: AcquireResult.Held(findSession(uuid)!![PlayerSessionTable.owner] ?: "")
            }
        }
    }

    override suspend fun renew(uuids: Collection<UUID>) {
        if (uuids.isEmpty()) return
        inTransaction {
            val until = leaseExpiry()
            PlayerSessionTable.update({ (PlayerSessionTable.uuid inList uuids) and (PlayerSessionTable.owner eq serverId) }) {
                it[leaseUntil] = until
            }
        }
    }

    override suspend fun commit(uuid: UUID, expectedVersion: Long): Long? = inTransaction {
        val until = leaseExpiry()
        val updated = PlayerSessionTable.update({
            (PlayerSessionTable.uuid eq uuid) and (PlayerSessionTable.owner eq serverId) and (PlayerSessionTable.version eq expectedVersion)
        }) {
            it[version] = expectedVersion + 1
            it[leaseUntil] = until
        }
        if (updated == 1) expectedVersion + 1 else null
    }

    override suspend fun release(uuid: UUID): Boolean = inTransaction {
        PlayerSessionTable.update({ (PlayerSessionTable.uuid eq uuid) and (PlayerSessionTable.owner eq serverId) }) {
            it[owner] = null
        } == 1
    }

    private fun Transaction.tryAcquire(uuid: UUID): AcquireResult.Acquired? {
        val until = leaseExpiry()
        val updated = PlayerSessionTable.update({
            (PlayerSessionTable.uuid eq uuid) and (
                PlayerSessionTable.owner.isNull() or
                    (PlayerSessionTable.owner eq serverId) or
                    PlayerSessionTable.leaseUntil.isNull() or
                    (PlayerSessionTable.leaseUntil less CurrentTimestamp<Instant>())
                )
        }) {
            it[owner] = serverId
            it[leaseUntil] = until
        }
        if (updated != 1) return null
        return AcquireResult.Acquired(findSession(uuid)!![PlayerSessionTable.version])
    }

    private fun Transaction.insertSession(uuid: UUID) {
        try {
            PlayerSessionTable.insert {
                it[PlayerSessionTable.uuid] = uuid
                it[owner] = serverId
                it[version] = 0
                it[leaseUntil] = leaseExpiry()
            }
        } catch (e: ExposedSQLException) {
            if (findSession(uuid) == null) throw e
        }
    }

    private fun findSession(uuid: UUID): ResultRow? =
        PlayerSessionTable.selectAll().where { PlayerSessionTable.uuid eq uuid }.singleOrNull()

    private fun leaseExpiry(): Expression<Instant?> {
        val seconds = lease.seconds
        val sql = when (val dialect = currentDialect) {
            is MysqlDialect -> "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL $seconds SECOND)"
            is H2Dialect -> "DATEADD('SECOND', $seconds, CURRENT_TIMESTAMP)"
            is SQLiteDialect -> "DATETIME('now', '+$seconds seconds')"
            else -> throw IllegalStateException("player session lease is not supported on ${dialect.name}")
        }
        return object : Expression<Instant?>() {
            override fun toQueryBuilder(queryBuilder: QueryBuilder) {
                queryBuilder.append(sql)
            }
        }
    }

    private suspend fun <T> inTransaction(block: Transaction.() -> T): T {
        val current = TransactionManager.currentOrNull()?.takeIf { it.db == database }
        return if (current != null) current.block() else newSuspendedTransaction(Dispatchers.IO, database) { block() }
    }
}
