package kr.hqservice.framework.database.component.datasource

import com.zaxxer.hikari.HikariConfig
import kr.hqservice.framework.database.DatabaseHost
import kr.hqservice.framework.database.util.buildHikariConfig

abstract class MySQLDataSource(
    private val databaseHost: DatabaseHost,
    private val poolName: String = "unnamed MySQLDataSource pool"
) : HQDataSource() {
    override fun getConfig(): HikariConfig {
        return buildHikariConfig {
            this.jdbcUrl = "jdbc:mysql://${databaseHost.host}:${databaseHost.port}/${databaseHost.database}?autoReconnect=true&allowMultiQueries=true"
            this.driverClassName = "com.mysql.cj.jdbc.Driver"
            this.username = databaseHost.user
            this.password = databaseHost.password
            this.connectionTestQuery = "SELECT 1"
            this.poolName = this@MySQLDataSource.poolName
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
        }
    }
}