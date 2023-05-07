package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.HQListener
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler
import org.bukkit.event.HandlerList

@ComponentHandler
class ListenerComponentHandler(private val plugin: HQPlugin) : HQComponentHandler<HQListener> {
    override fun setup(element: HQListener) {
        plugin.server.pluginManager.registerEvents(element, plugin)
    }

    override fun teardown(element: HQListener) {
        HandlerList.unregisterAll(element)
    }
}