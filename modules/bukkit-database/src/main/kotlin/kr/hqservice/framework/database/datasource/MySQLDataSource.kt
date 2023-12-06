package kr.hqservice.framework.database.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class MySQLDataSource(
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String,
    maximumPoolSize: Int
) : HikariDataSource(HikariConfig().apply {
    this.jdbcUrl = "jdbc:mysql://${host}:${port}/${database}?autoReconnect=true&allowMultiQueries=true"
    this.driverClassName = "com.mysql.cj.jdbc.Driver"
    this.username = user
    this.password = password
    this.connectionTestQuery = "SELECT 1"
    this.poolName = "hqframework"
    this.maximumPoolSize = maximumPoolSize
    addDataSourceProperty("cachePrepStmts", "true")
    addDataSourceProperty("prepStmtCacheSize", "250")
    addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    addDataSourceProperty("useServerPrepStmts", "true")
    addDataSourceProperty("useLocalSessionState", "true")
    addDataSourceProperty("rewriteBatchedStatements", "true")
    addDataSourceProperty("cacheResultSetMetadata", "true")
    addDataSourceProperty("cacheServerConfiguration", "true")
    addDataSourceProperty("elideSetAutoCommits", "true")
    addDataSourceProperty("maintainTimeStats", "false")
    addDataSourceProperty("characterEncoding", "utf8")
    addDataSourceProperty("useUnicode", "true")
})