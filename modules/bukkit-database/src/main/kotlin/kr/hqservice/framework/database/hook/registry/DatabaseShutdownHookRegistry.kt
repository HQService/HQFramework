package kr.hqservice.framework.database.hook.registry

import kr.hqservice.framework.database.hook.DatabaseShutdownHook
import javax.sql.DataSource

interface DatabaseShutdownHookRegistry {
    fun addHook(scope: DataSource.() -> Unit)

    fun addHook(databaseShutdownHook: DatabaseShutdownHook)

    fun getHooks(): List<DatabaseShutdownHook>
}