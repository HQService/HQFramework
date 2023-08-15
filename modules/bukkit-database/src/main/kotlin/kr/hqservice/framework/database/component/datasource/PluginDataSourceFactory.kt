package kr.hqservice.framework.database.component.datasource

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.HQInstanceFactory
import kr.hqservice.framework.bukkit.core.extension.getHQConfig
import kr.hqservice.framework.bukkit.core.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.database.util.getDatabaseHost
import kr.hqservice.framework.database.util.getDatabasePath
import kr.hqservice.framework.database.util.getDatabaseType
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.util.AnsiColor
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Component
class PluginDataSourceFactory(
    @Qualifier("database") private val coroutineScope: HQCoroutineScope
) : KoinComponent, HQInstanceFactory<HQDataSource> {
    private val dataSources: MutableMap<String, HQDataSource> = ConcurrentHashMap()

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

        fun logTeardown() {
            plugin.logger.info("${AnsiColor.GREEN}$dataSourceId DataSource 가 성공적으로 닫혔습니다.${AnsiColor.RESET}")
        }

        val dataSource = when (dataSourceId) {
            "mysql" -> {
                val host = if (configSection == null) {
                    plugin.getHQConfig().getDatabaseHost()
                } else {
                    plugin.getHQConfig().getDatabaseHost(configSection)
                }
                object : MySQLDataSource(host, poolName), CoroutineScope by coroutineScope {
                    override fun onTeardown() {
                        logTeardown()
                    }
                }
            }

            "sqlite" -> {
                val dbPath = if (configSection == null) {
                    plugin.getHQConfig().getDatabasePath()
                } else {
                    plugin.getHQConfig().getDatabasePath(configSection)
                }
                object : SQLiteDataSource(getSQLitePath(plugin, dbPath), poolName), CoroutineScope by coroutineScope {
                    override fun onTeardown() {
                        logTeardown()
                    }
                }
            }

            else -> {
                throw IllegalArgumentException("해당 '$dataSourceId' 아이디의 DataSource 는 지원하지 않습니다.")
            }
        }
        dataSources[dataSourceKey] = dataSource
        dataSource.setupDatabase()
        plugin.logger.info("${AnsiColor.GREEN}$dataSourceId DataSource 를 사용합니다.${AnsiColor.RESET}")
        return dataSource
    }

    private fun getSQLitePath(plugin: Plugin, databasePath: String): String {
        return "plugins/${plugin.dataFolder.nameWithoutExtension}/$databasePath"
    }

    private fun getDataSourceKey(pluginName: String, dataSourceId: String): String {
        return "$pluginName.$dataSourceId"
    }

    override fun createInstance(
        plugin: HQBukkitPlugin,
        annotatedElement: KAnnotatedElement,
        qualifier: org.koin.core.qualifier.Qualifier?,
        scopeQualifier: org.koin.core.qualifier.Qualifier?
    ): HQDataSource {
        return if (annotatedElement.hasAnnotation<DataSource>()) {
            val config = annotatedElement.findAnnotation<DataSource>()!!.configPath
            val type = plugin.getHQConfig().getString(config)
            getDataSource(plugin, type)
        } else if (qualifier == null) {
            throw IllegalArgumentException("Qualifier 을 찾을 수 없어 DataSource 생성에 실패하였습니다.")
        } else {
            getDataSource(plugin, qualifier.value)
        }
    }
}