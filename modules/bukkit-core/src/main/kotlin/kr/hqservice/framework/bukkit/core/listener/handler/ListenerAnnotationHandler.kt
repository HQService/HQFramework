package kr.hqservice.framework.bukkit.core.listener.handler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.service.ListenerService
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.EventExecutor

@AnnotationHandler
class ListenerAnnotationHandler(
    private val listenerService: ListenerService,
    private val plugin: HQBukkitPlugin
) : HQAnnotationHandler<Listener> {
    private val registeredListeners: MutableList<Pair<Any, org.bukkit.event.Listener>> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    override fun setup(instance: Any, annotation: Listener) {
        val pluginManager = plugin.server.pluginManager
        listenerService.createListener(instance, plugin).forEach { (eventClass, listeners) ->
            listeners.forEach { registeredListener ->
                pluginManager.registerEvent(
                    eventClass.java as Class<out Event>,
                    registeredListener.listener,
                    registeredListener.priority,
                    EventExecutor { _, event -> registeredListener.callEvent(event) },
                    plugin,
                    registeredListener.isIgnoringCancelled
                )
                registeredListeners.add(instance to registeredListener.listener)
            }
        }
    }

    override fun teardown(instance: Any, annotation: Listener) {
        val owned = registeredListeners.filter { it.first === instance }
        owned.forEach { (_, listener) -> HandlerList.unregisterAll(listener) }
        registeredListeners.removeIf { it.first === instance }
    }
}
