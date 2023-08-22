package kr.hqservice.framework.bukkit.scheduler

import com.zaxxer.hikari.HikariDataSource
import kr.hqservice.framework.bukkit.scheduler.database.provider.HQDataSourceConnectionProvider
import kr.hqservice.framework.database.exception.DataSourceClosedException
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.Singleton
import org.quartz.SchedulerFactory
import org.quartz.impl.StdSchedulerFactory
import java.util.*

@Configuration
class SchedulerConfig(private val dataSource: HikariDataSource) {
    @Singleton
    fun provideSchedulerFactory(): SchedulerFactory {
        if (dataSource.isClosed) {
            throw DataSourceClosedException(dataSource)
        }
        return StdSchedulerFactory(getSchedulerProperties())
    }

    private fun getSchedulerProperties(): Properties {
        return Properties().apply {
            // MainScheduler Properties
            this["org.quartz.scheduler.instanceName"] = "HQFrameworkClusteredScheduler"
            this["org.quartz.scheduler.instanceId"] = "AUTO"
            // ThreadPool
            this["org.quartz.threadPool.class"] = "org.quartz.simpl.SimpleThreadPool"
            this["org.quartz.threadPool.threadCount"] = "25"
            this["org.quartz.threadPool.threadPriority"] = "5"
            // Clustered JobStore
            this["org.quartz.jobStore.misfireThreshold"] = "1100"
            this["org.quartz.jobStore.class"] = "org.quartz.impl.jdbcjobstore.JobStoreTX"
            this["org.quartz.jobStore.useProperties"] = "false"
            this["org.quartz.jobStore.tablePrefix"] = "hqframework_quartz_"
            this["org.quartz.jobStore.isClustered"] = "true"
            this["org.quartz.jobStore.dataSource"] = "hqdatasource"
            this["org.quartz.jobStore.clusterCheckinInterval"] = "15000"
            this["org.quartz.dataSource.hqdatasource.connectionProvider.class"] = HQDataSourceConnectionProvider::class.qualifiedName
        }
    }
}