package kr.hqservice.framework.database.hook

import javax.sql.DataSource

interface DatabaseShutdownHook {
    fun shutdown(dataSource: DataSource)
}