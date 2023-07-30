package kr.hqservice.framework.bukkit.scheduler.config

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.scheduler.database.table.*
import kr.hqservice.framework.database.component.datasource.DataSource
import kr.hqservice.framework.database.component.datasource.HQDataSource
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import org.jetbrains.exposed.sql.SchemaUtils
import org.quartz.SchedulerFactory
import org.quartz.impl.StdSchedulerFactory
import java.util.*

@Configuration
class SchedulerConfig(
    @DataSource private val hqDataSource: HQDataSource
) {
    @Singleton
    fun provideSchedulerFactory(): SchedulerFactory {
        runBlocking {
            setupScheduler()
        }
        return StdSchedulerFactory(getSchedulerProperties())
    }

    @Singleton
    @Qualifier("hqframework.scheduler.datasource")
    fun provideSchedulerDataSource(): HQDataSource {
        return hqDataSource
    }

    private suspend fun setupScheduler() {
        hqDataSource.query {
            SchemaUtils.createMissingTablesAndColumns(
                QuartzJobDetailsTable,
                QuartzTriggersTable,
                QuartzSimpleTriggersTable,
                QuartzCronTriggersTable,
                QuartzSimpropTriggersTable,
                QuartzBlobTriggersTable,
                QuartzCalendarsTable,
                QuartzPausedTriggerGRPSTable,
                QuartzFiredTriggersTable,
                QuartzSchedulerStateTable,
                QuartzLocksTable)
        }
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
            this["org.quartz.jobStore.tablePrefix"] = "hqframework_QRTZ_"
            this["org.quartz.jobStore.isClustered"] = "true"
            this["org.quartz.jobStore.dataSource"] = "hqdatasource"
            this["org.quartz.jobStore.clusterCheckinInterval"] = "15000"
            this["org.quartz.dataSource.hqdatasource.connectionProvider.class"] = "kr.hqservice.framework.bukkit.scheduler.database.provider.HQDataSourceConnectionProvider"
        }
    }
}