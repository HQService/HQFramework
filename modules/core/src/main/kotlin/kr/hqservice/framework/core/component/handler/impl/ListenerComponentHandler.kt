package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.HQListener
import kr.hqservice.framework.core.component.handler.ComponentHandler
import org.bukkit.event.HandlerList
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [ComponentHandler::class])
@Named("listener")
class ListenerComponentHandler : ComponentHandler<HQListener> {
    override fun setup(element: HQListener, plugin: HQPlugin) {
        plugin.server.pluginManager.registerEvents(element, plugin)
    }

    override fun teardown(element: HQListener, plugin: HQPlugin) {
        HandlerList.unregisterAll(element)
    }
}