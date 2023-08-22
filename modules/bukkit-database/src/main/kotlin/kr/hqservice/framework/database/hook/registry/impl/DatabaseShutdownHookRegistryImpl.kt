package kr.hqservice.framework.database.hook.registry.impl

import kr.hqservice.framework.database.hook.DatabaseShutdownHook
import kr.hqservice.framework.database.hook.registry.DatabaseShutdownHookRegistry
import kr.hqservice.framework.global.core.component.Bean
import javax.sql.DataSource

@Bean
class DatabaseShutdownHookRegistryImpl : DatabaseShutdownHookRegistry {
    private val hooks: MutableList<DatabaseShutdownHook> = mutableListOf()

    override fun addHook(scope: DataSource.() -> Unit) {
        hooks.add(object : DatabaseShutdownHook {
            override fun shutdown(dataSource: DataSource) {
                scope(dataSource)
            }
        })
    }

    override fun addHook(databaseShutdownHook: DatabaseShutdownHook) {
        hooks.add(databaseShutdownHook)
    }

    override fun getHooks(): List<DatabaseShutdownHook> {
        return hooks
    }
}