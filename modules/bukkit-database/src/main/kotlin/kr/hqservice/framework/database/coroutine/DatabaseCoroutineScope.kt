package kr.hqservice.framework.database.coroutine

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import org.koin.core.annotation.Named
import java.util.logging.Level
import java.util.logging.Logger

@Named("database")
@HQSingleton(binds = [HQCoroutineScope::class, CoroutineScope::class])
@Component
class DatabaseCoroutineScope(plugin: HQBukkitPlugin, logger: Logger) : HQCoroutineScope(plugin, Dispatchers.IO) {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.log(Level.SEVERE, throwable) {
            "DatabaseCoroutineContext 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                    "job: ${context.job} \n" +
                    "stackTrace 를 출력합니다. \n"
        }
    }
    private val coroutineName = CoroutineName("DatabaseCoroutineContext")

    override fun getExceptionHandler(): CoroutineExceptionHandler {
        return exceptionHandler
    }

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }
}