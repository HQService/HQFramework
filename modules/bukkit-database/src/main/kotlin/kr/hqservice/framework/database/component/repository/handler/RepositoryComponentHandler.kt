package kr.hqservice.framework.database.component.repository.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.component.handler.CoroutineScopeComponentHandler
import kr.hqservice.framework.database.component.datasource.handler.DataSourceComponentHandler
import kr.hqservice.framework.database.component.repository.HQRepository
import kr.hqservice.framework.database.component.repository.Table
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import java.sql.SQLSyntaxErrorException
import java.util.logging.Logger
import kotlin.reflect.full.findAnnotation

@ComponentHandler(depends = [DataSourceComponentHandler::class, CoroutineScopeComponentHandler::class])
class RepositoryComponentHandler(
    @Named("main") private val mainCoroutineScope: CoroutineScope,
    private val logger: Logger
) : HQComponentHandler<HQRepository>, KoinComponent {
    override fun setup(element: HQRepository) {
        val table = element::class.findAnnotation<Table>() ?: return
        table.with.forEach {
            val tableInstance =
                it.objectInstance ?: throw IllegalArgumentException("Table 의 object instance 를 가져올 수 없습니다.")
            mainCoroutineScope.launch {
                element.dataSource.query {
                    try {
                        if (tableInstance.exists()) {
                            SchemaUtils.addMissingColumnsStatements(tableInstance, withLogs = false)
                        } else {
                            SchemaUtils.create(tableInstance)
                        }
                    } catch (exception: ExposedSQLException) {
                        if (exception.cause is SQLSyntaxErrorException) {
                            logger.info("${tableInstance.tableName} 테이블을 생성하는 과정에서 mariadb 내부 문제로 인해 테이블 생성을 실패하였을 수 있습니다.")
                        }
                    }
                }
            }
        }
    }
}