package kr.hqservice.framework.database

import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.CoroutineScopeAdvice
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.ExceptionHandler
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.MustBeStored
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.logging.Logger

@CoroutineScopeAdvice
class DatabaseExceptionHandler(private val logger: Logger) {
    @MustBeStored
    @ExceptionHandler
    fun handleExposedSQLException(exception: ExposedSQLException) {
        logger.severe("Unexpected ExposedSQLException occurred. handled but stored in the errors folder.")
        logger.severe("message: ${exception.message}")
        logger.severe("cause: ${exception.cause}")
        logger.severe("contexts:")
        exception.contexts.forEach {
            logger.severe(it.toString())
        }
        logger.severe("causedByQueries:")
        exception.causedByQueries().forEach {
            logger.severe(it)
        }
    }
}