package kr.hqservice.framework.coroutine.test

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger

@HQSingleton
@Component
class TestCoroutineScope(private val plugin: HQBukkitPlugin, private val logger: Logger) : HQCoroutineScope(plugin, Dispatchers.Default) {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.log(Level.SEVERE, throwable) {
            "TestCoroutineContext 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                    "job: ${context.job} \n" +
                    "scopeName: ${context.getScopeName()} \n" +
                    "stackTrace 를 출력합니다. \n"
        }
        throwable.printStackTrace()
    }

    private val coroutineName = CoroutineName("TestCoroutineContext")

    override fun getExceptionHandler(): CoroutineExceptionHandler {
        return exceptionHandler
    }

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }
}