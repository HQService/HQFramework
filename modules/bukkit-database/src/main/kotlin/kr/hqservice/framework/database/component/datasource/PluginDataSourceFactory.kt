package kr.hqservice.framework.database.component.datasource

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.HQInstanceFactory
import kr.hqservice.framework.bukkit.core.component.registry.InstanceFactoryRegistry
import kr.hqservice.framework.bukkit.core.extension.getHQConfig
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.database.util.getDatabaseHost
import kr.hqservice.framework.database.util.getDatabasePath
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.component.Qualifier
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap

@Component
class PluginDataSourceFactory(
    @Qualifier("database") private val coroutineScope: HQCoroutineScope,
    instanceFactoryRegistry: InstanceFactoryRegistry
) : KoinComponent, HQInstanceFactory<HQDataSource>, HQModule {
    private val dataSources: MutableMap<String, HQDataSource> = ConcurrentHashMap()

    init {
        instanceFactoryRegistry.registerInstanceFactory(this)
    }

    @Synchronized
    fun getDataSource(
        plugin: HQBukkitPlugin,
        dataSourceId: String,
        configSection: String? = null,
        poolName: String = "${plugin.name}.$dataSourceId",
        coroutineScope: CoroutineScope = this.coroutineScope
    ): HQDataSource {
        val dataSourceKey = getDataSourceKey(plugin.name, dataSourceId)
        if (dataSources[dataSourceKey] != null) {
            return dataSources[dataSourceKey]!!
        }

        val dataSource = when (dataSourceId) {
            "mysql" -> {
                val host = if (configSection == null) {
                    plugin.getHQConfig().getDatabaseHost()
                } else {
                    plugin.getHQConfig().getDatabaseHost(configSection)
                }
                object : MySQLDataSource(host, poolName), CoroutineScope by coroutineScope {}
            }

            "sqlite" -> {
                val dbPath = if (configSection == null) {
                    plugin.getHQConfig().getDatabasePath()
                } else {
                    plugin.getHQConfig().getDatabasePath(configSection)
                }
                object : SQLiteDataSource(dbPath, poolName), CoroutineScope by coroutineScope {}
            }

            else -> {
                throw IllegalArgumentException("해당 $dataSourceId 아이디의 DataSource 는 지원하지 않습니다.")
            }
        }
        dataSources[dataSourceKey] = dataSource
        return dataSource
    }

    private fun getDataSourceKey(pluginName: String, dataSourceId: String): String {
        return "$pluginName.$dataSourceId"
    }

    override fun createInstance(
        plugin: HQBukkitPlugin,
        qualifier: org.koin.core.qualifier.Qualifier?,
        scopeQualifier: org.koin.core.qualifier.Qualifier?
    ): HQDataSource {
        if (qualifier == null) {
            throw IllegalArgumentException("Qualifier 을 찾을 수 없어 DataSource 생성에 실패하였습니다.")
        }
        return getDataSource(plugin, qualifier.value)
    }
}