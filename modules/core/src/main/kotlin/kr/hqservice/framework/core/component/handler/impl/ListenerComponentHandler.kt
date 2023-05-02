package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.component.handler.ComponentHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [ComponentHandler::class])
@Named("listener")
class ListenerComponentHandler(private val plugin: Plugin) : ComponentHandler<Listener> {
    override fun setup(element: Listener) {
        plugin.server.pluginManager.registerEvents(element, plugin)
    }

    override fun teardown(element: Listener) {
        HandlerList.unregisterAll(element)
    }
}