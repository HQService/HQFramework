package kr.hqservice.framework.database.component.repository.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.coroutine.component.handler.CoroutineScopeComponentHandler
import kr.hqservice.framework.database.component.repository.HQRepository
import kr.hqservice.framework.database.component.repository.Table
import kr.hqservice.framework.database.component.datasource.handler.DataSourceComponentHandler
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import kotlin.reflect.full.findAnnotation

@ComponentHandler(depends = [DataSourceComponentHandler::class, CoroutineScopeComponentHandler::class])
class RepositoryComponentHandler(
    @Named("main") private val mainCoroutineScope: CoroutineScope
) : HQComponentHandler<HQRepository>, KoinComponent {
    override fun setup(element: HQRepository) {
        val table = element::class.findAnnotation<Table>() ?: return
        table.with.forEach {
            val tableInstance = it.objectInstance ?: throw IllegalArgumentException("Table 의 object instance 를 가져올 수 없습니다.")
            mainCoroutineScope.launch {
                element.getDataSource().query {
                    if(tableInstance.exists()) {
                        SchemaUtils.addMissingColumnsStatements(tableInstance, withLogs = false)
                    } else {
                        SchemaUtils.create(tableInstance)
                    }
                }
            }
        }
    }
}