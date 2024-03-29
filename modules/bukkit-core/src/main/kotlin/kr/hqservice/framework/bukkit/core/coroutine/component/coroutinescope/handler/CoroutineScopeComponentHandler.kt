package kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler

/**
 * 서버가 꺼질 때, Job 이 살아있다면,
 * 서버의 종료를 Job 이 종료될때까지 기다립니다.
 */
@ComponentHandler
class CoroutineScopeComponentHandler : HQComponentHandler<HQCoroutineScope> {
    override fun setup(element: HQCoroutineScope) {}

    override fun teardown(element: HQCoroutineScope) {
        runBlocking {
            element.getSupervisor().children.forEach { job ->
                job.join()
            }
        }
    }
}