package kr.hqservice.framework.coroutine

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.coroutine.extension.BukkitMain
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import org.koin.core.component.getScopeName
import java.util.logging.Level
import java.util.logging.Logger

@Qualifier("main")
@Component
@Singleton(binds = [HQCoroutineScope::class, CoroutineScope::class])
class BukkitMainCoroutineScope(plugin: HQBukkitPlugin, logger: Logger) :
    HQCoroutineScope(plugin, Dispatchers.BukkitMain) {
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