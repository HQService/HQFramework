package kr.hqservice.framework.coroutine.test

import kotlinx.coroutines.*
import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.coroutine.component.HQCoroutineContext
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

@HQSingleton
@Component
class TestCoroutineContext(private val logger: Logger) : HQCoroutineContext {
    override val coroutineContext: CoroutineContext
        get() = supervisorJob + Dispatchers.IO + CoroutineName("TestCoroutineContext") + CoroutineExceptionHandler { context, throwable ->
            logger.log(Level.SEVERE, throwable) {
                "TestCoroutineContext 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                        "job: ${context.job} \n" +
                        "scopeName: ${context.getScopeName()} \n" +
                        "stackTrace 를 출력합니다. \n"
            }
            throwable.printStackTrace()
        }

    private val supervisorJob = SupervisorJob()

    override fun getSupervisor(): Job {
        return supervisorJob
    }
}