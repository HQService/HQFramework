package kr.hqservice.framework.bukkit.core.listener.handler

import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.service.ListenerService
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import org.bukkit.event.HandlerList
import org.bukkit.plugin.SimplePluginManager

@AnnotationHandler
class ListenerAnnotationHandler(
    private val listenerService: ListenerService
) : HQAnnotationHandler<Listener> {
    override fun setup(instance: Any, annotation: Listener) {
        val plugin = PluginScopeFinder.get(instance::class)
        val registeredListeners = listenerService.createListener(instance, plugin)
        val eventListeners = SimplePluginManager::class.java.getDeclaredMethod("getEventListeners", Class::class.java)
        eventListeners.isAccessible = true
        registeredListeners.forEach { (clazz, listeners) ->
            val handlerList = eventListeners.invoke(plugin.server.pluginManager, clazz.java) as HandlerList
            handlerList.registerAll(listeners)
        }
    }
}