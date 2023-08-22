package kr.hqservice.framework.database.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class SQLiteDataSource(
    databasePath: String
) : HikariDataSource(HikariConfig().apply {
    this.jdbcUrl = "jdbc:sqlite:$databasePath"
    this.connectionTestQuery = "SELECT 1"
    this.poolName = "hqframework"
})