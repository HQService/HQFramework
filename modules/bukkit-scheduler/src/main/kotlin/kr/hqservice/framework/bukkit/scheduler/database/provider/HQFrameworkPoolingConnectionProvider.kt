package kr.hqservice.framework.bukkit.scheduler.database.provider

import kr.hqservice.framework.global.core.component.Bean
import org.koin.core.component.KoinComponent
import org.quartz.utils.PoolingConnectionProvider
import java.sql.Connection
import javax.sql.DataSource

@Bean
class HQFrameworkPoolingConnectionProvider(
    private val hqDataSource: DataSource
) : PoolingConnectionProvider, KoinComponent {
    override fun getConnection(): Connection {
       return hqDataSource.connection
    }

    override fun shutdown() {}

    override fun initialize() {}

    override fun getDataSource(): DataSource {
        return hqDataSource
    }
}