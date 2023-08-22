package kr.hqservice.framework.bukkit.scheduler.component.handler

import kr.hqservice.framework.bukkit.scheduler.component.HQTriggerListener
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.quartz.ListenerManager

@ComponentHandler
class HQTriggerListenerComponentHandler(private val listenerManager: ListenerManager) : HQComponentHandler<HQTriggerListener> {
    override fun setup(element: HQTriggerListener) {
        listenerManager.addTriggerListener(element)
    }

    override fun teardown(element: HQTriggerListener) {
        listenerManager.removeTriggerListener(element.name)
    }
}