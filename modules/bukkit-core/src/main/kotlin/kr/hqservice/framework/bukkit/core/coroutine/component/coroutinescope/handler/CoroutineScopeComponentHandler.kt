package kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.coroutine.CoroutineTeardown
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

        if (supervisor.children.none()) return

        runBlocking {
            CoroutineTeardown.awaitChildren(supervisor, logger)
        }
    }
}
