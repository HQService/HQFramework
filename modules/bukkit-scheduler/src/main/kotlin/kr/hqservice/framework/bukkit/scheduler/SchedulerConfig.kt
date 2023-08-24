package kr.hqservice.framework.bukkit.scheduler

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.database.exception.DataSourceClosedException
import kr.hqservice.framework.database.hook.registry.DatabaseShutdownHookRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.util.AnsiColor
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import org.quartz.ListenerManager
import org.quartz.Scheduler
import org.quartz.impl.DirectSchedulerFactory
import org.quartz.impl.jdbcjobstore.JobStoreTX
import org.quartz.simpl.SimpleThreadPool
import org.quartz.spi.JobStore
import org.quartz.spi.ThreadPool
import org.quartz.utils.ConnectionProvider
import org.quartz.utils.DBConnectionManager

@Configuration
class SchedulerConfig(
    private val hikariDataSource: HikariDataSource,
    private val server: Server,
    private val plugin: HQBukkitPlugin
) {
    private companion object {
        const val SCHEDULER_NAME = "HQFrameworkScheduler"
        const val DATASOURCE_NAME = "HQFrameworkDataSource"
        const val TABLE_PREFIX = "hqframework_quartz_"
    }

    @Bean
    fun provideListenerManager(scheduler: Scheduler): ListenerManager {
        return scheduler.listenerManager
    }

    @Bean
    fun provideScheduler(
        threadPool: ThreadPool,
        jobStore: JobStore,
        databaseShutdownHookRegistry: DatabaseShutdownHookRegistry
    ): Scheduler {
        if (hikariDataSource.isClosed) {
            throw DataSourceClosedException(hikariDataSource)
        }
        DirectSchedulerFactory
            .getInstance()
            .createScheduler(
                SCHEDULER_NAME, getSchedulerInstanceId(), threadPool, jobStore,
                mapOf(),
                null, 0,
                -1, 15000,
                false, null
            )

        return DirectSchedulerFactory.getInstance().getScheduler(SCHEDULER_NAME).also { scheduler ->
            databaseShutdownHookRegistry.addHook {
                plugin.logger.info("${AnsiColor.CYAN}Shutting down Scheduler...${AnsiColor.RESET}")
                scheduler.shutdown(true)
            }
            plugin.launch {
                bukkitDelay(1)
                scheduler.start()
            }
        }
    }

    @Bean
    fun provideJobStore(connectionProvider: ConnectionProvider, config: HQYamlConfiguration): JobStore {
        DBConnectionManager.getInstance().addConnectionProvider(DATASOURCE_NAME, connectionProvider)
        return JobStoreTX().apply {
            misfireThreshold = config.getLong("scheduler.job-store.misfire-threshold", 1100)
            clusterCheckinInterval = config.getLong("scheduler.job-store.cluster-checkin-interval", 2000)
            tablePrefix = TABLE_PREFIX
            dataSource = DATASOURCE_NAME
            dbRetryInterval = 15000
            instanceId = getSchedulerInstanceId()
            isThreadsInheritInitializersClassLoadContext = true
            setUseProperties(config.getBoolean("scheduler.job-store.use-properties", true).toString())
            setIsClustered(config.getBoolean("scheduler.job-store.is-clustered", true))
        }
    }

    @Bean
    fun provideSchedulerThreadPool(config: HQYamlConfiguration): ThreadPool {
        return SimpleThreadPool(
            config.getInt("scheduler.thread-pool.thread-count", 10),
            config.getInt("scheduler.thread-pool.thread-priority", Thread.NORM_PRIORITY)
        ).apply {
            isThreadsInheritContextClassLoaderOfInitializingThread = true
            isThreadsInheritGroupOfInitializingThread = true
            Thread.currentThread().contextClassLoader = plugin.getPluginClassLoader()
            initialize()
        }
    }

    private fun getSchedulerInstanceId(): String {
        return server.port.toString()
    }
}