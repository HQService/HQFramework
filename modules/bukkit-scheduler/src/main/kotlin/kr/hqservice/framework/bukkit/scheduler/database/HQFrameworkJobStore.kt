package kr.hqservice.framework.bukkit.scheduler.database

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.util.AnsiColor
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import org.quartz.impl.jdbcjobstore.JobStoreTX
import org.quartz.utils.ConnectionProvider
import org.quartz.utils.DBConnectionManager
import org.slf4j.LoggerFactory
import java.util.logging.Logger

/**
 * Quartz 는 SQLite JobStoreTX 를 지원하지 않는다.
 * SQLite 는 write transaction 일 때 lock 을 걸게 되는데, lock 을 건 동안 trigger 가 발동될 경우 SQLITE_BUSY 오류를 발생시킨다.
 * 그렇기 때문에 isAcquireTriggersWithinLock 으로 먼저 Lock 을 검사한다.
 */
@Bean
class HQFrameworkJobStore(
    config: HQYamlConfiguration,
    server: Server,
    logger: Logger,
    connectionProvider: ConnectionProvider
) : JobStoreTX() {
    private companion object {
        const val DATASOURCE_NAME = "HQFrameworkDataSource"
        const val TABLE_PREFIX = "hqframework_quartz_"
    }

    override fun getLog(): org.slf4j.Logger {
        return LoggerFactory.getLogger("HQFramework")
    }

    init {
        if (config.getString("database.type").uppercase() == "SQLITE") {
            driverDelegateClass = SQLiteDriverDelegate::class.qualifiedName
            logger.info("${AnsiColor.CYAN}SQLite DataSource Provided to HQFrameworkJobStore.${AnsiColor.RESET}")
            logger.info("${AnsiColor.CYAN}Set job store driver delegate class to SQLiteDriverDelegate.${AnsiColor.RESET}")
            this.isAcquireTriggersWithinLock = true
        }
        DBConnectionManager.getInstance().addConnectionProvider(DATASOURCE_NAME, connectionProvider)
        misfireThreshold = config.getLong("scheduler.job-store.misfire-threshold", 1100)
        clusterCheckinInterval = config.getLong("scheduler.job-store.cluster-checkin-interval", 2000)
        tablePrefix = TABLE_PREFIX
        this.dataSource = DATASOURCE_NAME
        dbRetryInterval = 15000
        instanceId = getSchedulerInstanceId(server)
        isThreadsInheritInitializersClassLoadContext = true
        setUseProperties(config.getBoolean("scheduler.job-store.use-properties", true).toString())
        setIsClustered(config.getBoolean("scheduler.job-store.is-clustered", true))
    }

    private fun getSchedulerInstanceId(server: Server): String {
        return server.port.toString()
    }
}