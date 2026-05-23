package kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.handler

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.childrenAll
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
        const val GRACE_PERIOD_MS = 5000L
        const val FORCE_CANCEL_TIMEOUT_MS = 2000L
        val logger: Logger = Logger.getLogger("HQFramework.CoroutineScope")
    }

    override fun setup(element: HQCoroutineScope) {}

    override fun teardown(element: HQCoroutineScope) {
        val supervisor = element.getSupervisor()

        // opt-in 잡 즉시 cancel
        supervisor.childrenAll
            .filter { job ->
                job.coroutineContext[TeardownOptionCoroutineContextElement.Key]?.cancelWhenPluginTeardown == true
            }.forEach { it.cancel() }

        val children = supervisor.children.toList()
        if (children.isEmpty()) return

        runBlocking {
            withTimeoutOrNull(GRACE_PERIOD_MS) {
                coroutineScope {
                    children.forEach { job -> launch { job.join() } }
                }
            }

            val stillActive = children.filter { it.isActive }
            if (stillActive.isNotEmpty()) {
                stillActive.forEach { it.cancel() }
                withTimeoutOrNull(FORCE_CANCEL_TIMEOUT_MS) {
                    coroutineScope {
                        stillActive.forEach { job -> launch { job.join() } }
                    }
                }

                children.filter { it.isActive }.forEach { job ->
                    val name = job.coroutineContext[CoroutineName]?.name ?: "unnamed"
                    logger.warning("Abandoning coroutine [$name] during teardown - non-cancellable blocking work detected")
                }
            }
        }
    }
}
