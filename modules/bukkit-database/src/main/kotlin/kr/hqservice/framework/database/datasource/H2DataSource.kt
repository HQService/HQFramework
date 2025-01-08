package kr.hqservice.framework.database.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class H2DataSource(
    databasePath: String,
    inMemory: Boolean = false
) : HikariDataSource(HikariConfig().apply {
    this.driverClassName = "org.h2.Driver"
    this.jdbcUrl = if (inMemory) {
        "jdbc:h2:mem:$databasePath;DB_CLOSE_DELAY=-1"
    } else {
        "jdbc:h2:file:$databasePath"
    }
    this.connectionTestQuery = "SELECT 1"
    this.poolName = "h2framework"
})