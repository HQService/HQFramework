package kr.hqservice.framework.inventory.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger

@Qualifier("url-reader")
@Singleton(binds = [HQCoroutineScope::class])
@Component
class URLReaderCoroutineScope(plugin: HQBukkitPlugin, logger: Logger) : HQCoroutineScope(plugin, Dispatchers.IO) {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.log(Level.SEVERE, throwable) {
            "URLReaderCoroutineContext 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                    "job: ${context.job} \n" +
                    "scopeName: ${context.getScopeName()} \n" +
                    "stackTrace 를 출력합니다. \n"
        }
    }
    private val coroutineName = CoroutineName("URLReaderCoroutinContext")

    override fun getExceptionHandler(): CoroutineExceptionHandler {
        return exceptionHandler
    }

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }
}