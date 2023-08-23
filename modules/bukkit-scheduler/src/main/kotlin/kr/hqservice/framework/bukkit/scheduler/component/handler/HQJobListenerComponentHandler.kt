package kr.hqservice.framework.bukkit.scheduler.component.handler

import kr.hqservice.framework.bukkit.scheduler.component.HQJobListener
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.quartz.ListenerManager

@ComponentHandler
class HQJobListenerComponentHandler(private val listenerManager: ListenerManager) : HQComponentHandler<HQJobListener> {
    override fun setup(element: HQJobListener) {
        listenerManager.addJobListener(element)
    }

    override fun teardown(element: HQJobListener) {
        listenerManager.removeJobListener(element.name)
    }
}