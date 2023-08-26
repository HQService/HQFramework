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
import org.bukkit.Server
import org.quartz.ListenerManager
import org.quartz.Scheduler
import org.quartz.impl.DirectSchedulerFactory

@Configuration
class SchedulerConfig(
    private val hikariDataSource: HikariDataSource,
    private val plugin: HQBukkitPlugin
) {
    private companion object {
        const val SCHEDULER_NAME = "HQFrameworkScheduler"
    }

    @Bean
    fun provideListenerManager(scheduler: Scheduler): ListenerManager {
        return scheduler.listenerManager
    }

    @Bean
    fun provideScheduler(
        threadPool: SchedulerThreadPool,
        jobStore: PersistenceJobStore,
        databaseShutdownHookRegistry: DatabaseShutdownHookRegistry,
        server: Server
    ): Scheduler {
        if (hikariDataSource.isClosed) {
            throw DataSourceClosedException(hikariDataSource)
        }
        DirectSchedulerFactory
            .getInstance()
            .createScheduler(
                SCHEDULER_NAME, server.port.toString(), threadPool, jobStore,
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
}