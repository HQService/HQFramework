package kr.hqservice.framework.nms.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.Singleton
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import org.koin.core.annotation.Named
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger

@Named("packet")
@Singleton(binds = [HQCoroutineScope::class])
@Component
class PacketCoroutineScope(plugin: HQPlugin, logger: Logger) : HQCoroutineScope(plugin, Dispatchers.IO) {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.log(Level.SEVERE, throwable) {
            "PacketCoroutineContext 에서 오류 ${throwable::class.simpleName} 이(가) 발생하였습니다. \n" +
                    "job: ${context.job} \n" +
                    "scopeName: ${context.getScopeName()} \n" +
                    "stackTrace 를 출력합니다. \n"
        }
    }
    private val coroutineName = CoroutineName("PacketCoroutineContext")

    override fun getExceptionHandler(): CoroutineExceptionHandler {
        return exceptionHandler
    }

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }
}