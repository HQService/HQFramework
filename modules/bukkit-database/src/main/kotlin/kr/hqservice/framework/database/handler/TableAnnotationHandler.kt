package kr.hqservice.framework.database.handler

import kr.hqservice.framework.database.Table
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import java.sql.SQLSyntaxErrorException
import java.util.logging.Logger

@AnnotationHandler
class TableAnnotationHandler(
    private val database: Database,
    private val logger: Logger
) : HQAnnotationHandler<Table>, KoinComponent {
    override fun setup(instance: Any, annotation: Table) {
        if (instance !is org.jetbrains.exposed.sql.Table) {
            throw IllegalStateException("@Table annotations are for Exposed tables.")
        }

        transaction(database) {
            try {
                if (instance.exists()) {
                    SchemaUtils.addMissingColumnsStatements(instance, withLogs = false)
                } else {
                    SchemaUtils.create(instance)
                }
            } catch (exception: SQLSyntaxErrorException) {
                logger.info("${instance.tableName} table may initialized not properly due to Mariadb internal issues.")
            }
        }
    }
}