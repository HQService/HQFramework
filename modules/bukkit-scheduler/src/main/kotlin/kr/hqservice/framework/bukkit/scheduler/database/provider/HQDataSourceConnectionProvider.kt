package kr.hqservice.framework.bukkit.scheduler.database.provider

import kr.hqservice.framework.database.component.datasource.HQDataSource
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject
import org.quartz.utils.ConnectionProvider
import java.sql.Connection

class HQDataSourceConnectionProvider : ConnectionProvider {
    private val dataSource: HQDataSource by inject(HQDataSource::class.java, named("hqframework.scheduler.datasource"))

    override fun getConnection(): Connection {
       return dataSource.getHikariDataSource().connection
    }

    override fun shutdown() {
        dataSource.getHikariDataSource().close()
    }

    override fun initialize() {
        dataSource.getHikariDataSource()
    }
}