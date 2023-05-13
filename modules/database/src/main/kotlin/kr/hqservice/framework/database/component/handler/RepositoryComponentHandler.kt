package kr.hqservice.framework.database.component.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler
import kr.hqservice.framework.coroutine.component.handler.CoroutineScopeComponentHandler
import kr.hqservice.framework.database.component.HQRepository
import org.jetbrains.exposed.sql.SchemaUtils
import org.koin.core.component.KoinComponent

@ComponentHandler(depends = [DataSourceComponentHandler::class, EntityClassComponentHandler::class, CoroutineScopeComponentHandler::class])
class RepositoryComponentHandler : HQComponentHandler<HQRepository<*, *>>, KoinComponent {
    override fun setup(element: HQRepository<*, *>) {
        runBlocking {
            element.getDataSource().query {
                SchemaUtils.createMissingTablesAndColumns(element.getEntityClass().table)
            }
        }
    }
}