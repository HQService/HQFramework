package kr.hqservice.framework.coroutine

import kotlinx.coroutines.*
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.coroutine.component.HQCoroutineContext
import org.koin.core.component.getScopeName
import java.util.logging.Level

@Component
@HQSingleton
class BukkitMainCoroutineContext(plugin: HQPlugin) : HQCoroutineContext(plugin, Dispatchers.Main) {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        plugin.logger.log(Level.SEVERE, throwable) {
            "BukkitMainCoroutineContext 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                    "job: ${context.job} \n" +
                    "scopeName: ${context.getScopeName()} \n" +
                    "stackTrace 를 출력합니다. \n"
        }
    }

    private val coroutineName = CoroutineName("BukkitMainCoroutineContext")

    override fun getExceptionHandler(): CoroutineExceptionHandler {
        return exceptionHandler
    }

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }
}