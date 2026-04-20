package kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.handler

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.bukkit.core.coroutine.extension.coroutineContext
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import java.util.logging.Logger

/**
 * 서버가 꺼질 때, Job 이 살아있다면,
 * 서버의 종료를 Job 이 종료될때까지 기다립니다.
 */
@ComponentHandler
class CoroutineScopeComponentHandler : HQComponentHandler<HQCoroutineScope> {
    private companion object {
        const val JOIN_TIMEOUT_MS = 2000L
        val logger: Logger = Logger.getLogger("HQFramework.CoroutineScope")
    }

    override fun setup(element: HQCoroutineScope) {}

    override fun teardown(element: HQCoroutineScope) {
        runBlocking {
            element.getSupervisor().children.toList().forEach { job ->
                val name = job.coroutineContext[CoroutineName]?.name ?: "unnamed"
                if (withTimeoutOrNull(JOIN_TIMEOUT_MS) { job.join() } == null) {
                    job.cancel()
                    if (withTimeoutOrNull(JOIN_TIMEOUT_MS) { job.join() } == null) {
                        logger.warning("Abandoning coroutine [$name] during teardown - non-cancellable blocking work detected")
                    }
                }
            }
        }
    }
}
