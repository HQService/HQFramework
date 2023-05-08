package kr.hqservice.framework.coroutine.test

import kotlinx.coroutines.*
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.coroutine.component.HQCoroutineContext
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger

@HQSingleton
@Component
class TestCoroutineContext(private val plugin: HQPlugin, private val logger: Logger) : HQCoroutineContext(plugin, Dispatchers.Default) {
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