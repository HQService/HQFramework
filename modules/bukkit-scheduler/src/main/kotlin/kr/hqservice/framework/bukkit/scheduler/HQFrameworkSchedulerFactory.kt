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
import org.quartz.SchedulerException
import org.quartz.SchedulerFactory
import org.quartz.core.JobRunShellFactory
import org.quartz.core.QuartzSchedulerResources
import org.quartz.impl.*
import org.quartz.spi.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.logging.Level
import java.util.logging.Logger

@Bean
class HQFrameworkSchedulerFactory(
    private val logger: Logger,
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
            schedulerPluginMap.forEach { (_, plugin) ->
                addSchedulerPlugin(plugin)
            }
        }

        setDetails(threadPool, schedulerName, schedulerInstanceId)
        val coreScheduler = HQFrameworkSchedulerCore(quartzSchedulerResources, jobFactory, logger)
        setDetails(jobStore, schedulerName, schedulerInstanceId)
        jobStore.initialize(classLoadHelper, coreScheduler.schedulerSignaler)
        val scheduler: Scheduler = StdScheduler(coreScheduler)
        jobRunShellFactory.initialize(scheduler)
        coreScheduler.initialize()
        schedulerPluginMap.forEach { (key, schedulerPlugin) ->
            schedulerPlugin.initialize(key, scheduler, classLoadHelper)
        }
        logger.info("${AnsiColor.CYAN}Quartz engine version: ${coreScheduler.version}")
        coreScheduler.addNoGCObject(schedulerRepository)
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

    private fun setDetails(
        target: Any,
        schedulerName: String,
        schedulerId: String
    ) {
        setFieldBySetter(target, "setInstanceName", schedulerName)
        setFieldBySetter(target, "setInstanceId", schedulerId)
    }

    private fun setFieldBySetter(target: Any, method: String, value: String) {
        val setter: Method
        try {
            setter = target.javaClass.getMethod(method, String::class.java)
        } catch (e: SecurityException) {
            logger.log(Level.SEVERE, e) {
                "A SecurityException occured: ${e.message}"
            }
            return
        } catch (e: NoSuchMethodException) {
            logger.log(Level.SEVERE, e) {
                "${target.javaClass.getName()} does not contain public method " + method + "(String)"
            }
            return
        }
        if (Modifier.isAbstract(setter.modifiers)) {
            logger.severe((target.javaClass.getName()
                    + " does not implement " + method
                    + "(String)")
            )
            return
        }
        try {
            setter.invoke(target, value)
        } catch (ite: InvocationTargetException) {
            throw SchedulerException(ite.targetException)
        } catch (e: Exception) {
            throw SchedulerException(e)
        }
    }
}