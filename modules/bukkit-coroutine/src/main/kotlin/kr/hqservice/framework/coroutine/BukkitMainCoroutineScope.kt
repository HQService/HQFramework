package kr.hqservice.framework.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.coroutine.dispatcher.BukkitMainDispatcher
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import org.koin.core.annotation.Named
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger

@Named("main")
@Component
@HQSingleton(binds = [HQCoroutineScope::class, CoroutineScope::class])
class BukkitMainCoroutineScope(plugin: HQBukkitPlugin, logger: Logger) :
    HQCoroutineScope(plugin, BukkitMainDispatcher) {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.log(Level.SEVERE, throwable) {
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