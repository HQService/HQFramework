package kr.hqservice.framework.bungee.core.component.handler

import kr.hqservice.framework.bungee.core.component.HQListener
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager

@ComponentHandler
class ListenerComponentHandler(
    private val plugin: Plugin,
    private val pluginManager: PluginManager
) : HQComponentHandler<HQListener> {
    override fun setup(element: HQListener) {
        pluginManager.registerListener(plugin, element)
    }

    override fun teardown(element: HQListener) {
        pluginManager.unregisterListener(element)
    }
}