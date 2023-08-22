package kr.hqservice.framework.bukkit.scheduler.database.provider

import org.koin.java.KoinJavaComponent.inject
import org.quartz.utils.ConnectionProvider
import java.sql.Connection
import javax.sql.DataSource

class HQDataSourceConnectionProvider : ConnectionProvider {
    private val dataSource: DataSource by inject(DataSource::class.java)

    override fun getConnection(): Connection {
       return dataSource.connection
    }

    override fun shutdown() {}

    override fun initialize() {}
}