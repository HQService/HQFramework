package kr.hqservice.framework.database.component

import com.zaxxer.hikari.HikariConfig
import kr.hqservice.framework.database.util.buildHikariConfig

abstract class SQLiteDataSource(
    private val databasePath: String,
    private val poolName: String = "unnamed SQLiteDataSource pool"
) : HQDataSource() {
    override fun getConfig(): HikariConfig {
        return buildHikariConfig {
            this.dataSourceClassName = "org.sqlite.SQLiteDataSource"
            this.jdbcUrl = "jdbc:sqlite:${this@SQLiteDataSource.databasePath}"
            this.connectionTestQuery = "SELECT 1"
            this.poolName = this@SQLiteDataSource.poolName
        }
    }
}