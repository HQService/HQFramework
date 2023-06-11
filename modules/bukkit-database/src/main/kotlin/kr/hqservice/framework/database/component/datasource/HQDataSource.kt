package kr.hqservice.framework.database.component.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.global.core.component.HQComponent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

abstract class HQDataSource : HQComponent, CoroutineScope {
    private val hikariDataSourceLazy = lazy { HikariDataSource(getConfig()) }
    private val databaseLazy = lazy { Database.connect(hikariDataSourceLazy.value) }

    fun setupDatabase() {
        databaseLazy.value
    }

    private fun getDatabase(): Database {
        return databaseLazy.value
    }

    suspend fun <T> query(block: suspend (Transaction) -> T): T {
        return newSuspendedTransaction(coroutineContext, getDatabase()) {
            block(this)
        }
    }

    abstract fun getConfig(): HikariConfig

    fun teardown() {
        TransactionManager.closeAndUnregister(databaseLazy.value)
        hikariDataSourceLazy.value.close()
    }
}
