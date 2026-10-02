package kr.hqservice.framework.database.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class SQLiteDataSource(
    databasePath: String
) : HikariDataSource(HikariConfig().apply {
    this.driverClassName = "org.sqlite.JDBC"
    this.jdbcUrl = "jdbc:sqlite:$databasePath"
    this.connectionTestQuery = "SELECT 1"
    this.poolName = "hqframework"
    this.maximumPoolSize = 1
    addDataSourceProperty("journal_mode", "WAL")
    addDataSourceProperty("busy_timeout", "5000")
})