package kr.hqservice.framework.bukkit.core.component.handler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.bukkit.event.HandlerList

@ComponentHandler
class ListenerComponentHandler(private val plugin: HQBukkitPlugin) : HQComponentHandler<HQListener> {
    override fun setup(element: HQListener) {
        plugin.server.pluginManager.registerEvents(element, plugin)
    }

    override fun teardown(element: HQListener) {
        HandlerList.unregisterAll(element)
    }
}