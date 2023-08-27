package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import org.quartz.impl.jdbcjobstore.JobStoreTX
import org.quartz.utils.ConnectionProvider
import org.quartz.utils.DBConnectionManager

@Bean
class HQFrameworkJobStore(
    config: HQYamlConfiguration,
    server: Server
) : JobStoreTX() {
    private companion object {
        const val DATASOURCE_NAME = "HQFrameworkDataSource"
        const val TABLE_PREFIX = "hqframework_quartz_"
    }

    init {
        DBConnectionManager.getInstance().addConnectionProvider(DATASOURCE_NAME, connectionProvider)
        misfireThreshold = config.getLong("scheduler.job-store.misfire-threshold", 1100)
        clusterCheckinInterval = config.getLong("scheduler.job-store.cluster-checkin-interval", 2000)
        tablePrefix = TABLE_PREFIX
        dataSource = DATASOURCE_NAME
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