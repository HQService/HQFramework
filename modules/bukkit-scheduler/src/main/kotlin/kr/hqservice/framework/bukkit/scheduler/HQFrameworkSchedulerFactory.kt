package kr.hqservice.framework.bukkit.scheduler

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.database.exception.DataSourceClosedException
import kr.hqservice.framework.database.hook.registry.DatabaseShutdownHookRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.util.AnsiColor
import org.bukkit.Server
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.core.JobRunShellFactory
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources
import org.quartz.impl.*
import org.quartz.spi.*
import java.util.logging.Logger

@Bean
class HQFrameworkSchedulerFactory(
    private val logger: Logger,
    private val schedulerDetailsSetter: HQFrameworkSchedulerDetailsSetter,
    private val threadPool: ThreadPool,
    private val jobStore: JobStore,
    private val jobFactory: JobFactory,
    private val jobRunShellFactory: JobRunShellFactory,
    private val schedulerRepository: SchedulerRepository,
    private val classLoadHelper: ClassLoadHelper,
    private val threadExecutor: ThreadExecutor,
    private val databaseShutdownHookRegistry: DatabaseShutdownHookRegistry,
    private val server: Server,
    private val plugin: HQBukkitPlugin,
    private val hikariDataSource: HikariDataSource
) : SchedulerFactory {
    private var initialized = false

    private companion object Options {
        const val SCHEDULER_NAME = "HQFrameworkScheduler"
        const val DB_FAILURE_RETRY_INTERVAL = 15000L
        const val IDLE_WAIT_TIME = -1L
        const val MAKE_SCHEDULER_THREAD_DAEMON = false
        const val MAX_BATCH_SIZE = 1
        const val DEFAULT_BATCH_TIME_WINDOW = 0L
    }

    @Synchronized
    private fun createScheduler(): Scheduler {
        if (hikariDataSource.isClosed) {
            throw DataSourceClosedException(hikariDataSource)
        }
        val schedulerName = SCHEDULER_NAME
        val schedulerInstanceId = server.port.toString()
        val schedulerPluginMap: Map<String, SchedulerPlugin> = mapOf()

        threadPool.setInstanceName(schedulerName)
        threadPool.initialize()

        val quartzSchedulerResources = QuartzSchedulerResources().apply {
            setName(schedulerName)
            setInstanceId(schedulerInstanceId)
            makeSchedulerThreadDaemon = MAKE_SCHEDULER_THREAD_DAEMON
            setJobRunShellFactory(this@HQFrameworkSchedulerFactory.jobRunShellFactory)
            setThreadPool(this@HQFrameworkSchedulerFactory.threadPool)
            threadExecutor = this@HQFrameworkSchedulerFactory.threadExecutor
            setJobStore(this@HQFrameworkSchedulerFactory.jobStore)
            maxBatchSize = MAX_BATCH_SIZE
            batchTimeWindow = DEFAULT_BATCH_TIME_WINDOW
            rmiRegistryHost = null
            rmiRegistryPort = 0
            jmxExport = false
        }

        schedulerDetailsSetter.setDetails(threadPool, schedulerName, schedulerInstanceId)
        schedulerPluginMap.forEach { (_, plugin) ->
            quartzSchedulerResources.addSchedulerPlugin(plugin)
        }
        val quartzScheduler = QuartzScheduler(quartzSchedulerResources, IDLE_WAIT_TIME, DB_FAILURE_RETRY_INTERVAL)
        schedulerDetailsSetter.setDetails(jobStore, schedulerName, schedulerInstanceId)
        jobStore.initialize(classLoadHelper, quartzScheduler.schedulerSignaler)
        val scheduler: Scheduler = StdScheduler(quartzScheduler).apply {
            setJobFactory(jobFactory)
        }
        jobRunShellFactory.initialize(scheduler)
        quartzScheduler.initialize()
        schedulerPluginMap.forEach { (key, schedulerPlugin) ->
            schedulerPlugin.initialize(key, scheduler, classLoadHelper)
        }
        logger.info("${AnsiColor.CYAN}Quartz engine version: ${quartzScheduler.version}")
        quartzScheduler.addNoGCObject(schedulerRepository)
        schedulerRepository.bind(scheduler)
        databaseShutdownHookRegistry.addHook {
            plugin.logger.info("${AnsiColor.CYAN}Shutting down Scheduler...${AnsiColor.RESET}")
            scheduler.shutdown(true)
        }
        plugin.launch {
            bukkitDelay(1)
            scheduler.start()
        }
        initialized = true
        return scheduler
    }

    override fun getScheduler(): Scheduler {
        if (!initialized) {
            return createScheduler()
        }
        return getScheduler(SCHEDULER_NAME)
    }

    override fun getScheduler(schedName: String?): Scheduler {
        return schedulerRepository.lookup(schedName)
    }

    override fun getAllSchedulers(): MutableCollection<Scheduler> {
        return schedulerRepository.lookupAll()
    }
}