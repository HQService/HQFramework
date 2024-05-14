package kr.hqservice.framework.bukkit.core.listener.service

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.listener.SuspendEventExecutor
import kr.hqservice.framework.bukkit.core.listener.exception.ListenerRegistrationFailedException
import kr.hqservice.framework.global.core.component.Service
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.RegisteredListener
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

@Service
class ListenerService {
    fun createListener(instance: Any, plugin: HQBukkitPlugin): Map<KClass<*>, MutableCollection<RegisteredListener>> {
        if (instance::class.declaredFunctions.any { it.hasAnnotation<EventHandler>()} ) {
            plugin.logger.severe("Use @Subscribe annotation instead of @EventHandler.")
        }
        return instance::class.declaredFunctions
            .filter {
                it.hasAnnotation<Subscribe>()
            }.filter {
                it.javaMethod?.isBridge != true && it.javaMethod?.isSynthetic != true
            }.onEach {
                if (!it.isAccessible) {
                    it.isAccessible = true
                }
            }.groupBy {
                val firstParameter = it.javaMethod!!.parameters.firstOrNull()?.type?.kotlin
                if (firstParameter?.isSubclassOf(Event::class) == false) {
                    throw ListenerRegistrationFailedException(instance::class, "first parameter of event handler must be event.")
                }
                firstParameter!!
            }.mapValues { (event, kFunctions) ->
                kFunctions.map { kFunction ->
                    val executor = SuspendEventExecutor(event, instance, kFunction, plugin)
                    val annotation = kFunction.findAnnotation<Subscribe>()!!
                    RegisteredListener(object : Listener {}, executor, getBukkitEventPriority(annotation.handleOrder), plugin, annotation.ignoreCancelled)
                }.toMutableList()
            }
    }

    private fun getBukkitEventPriority(handleOrder: HandleOrder): EventPriority {
        return when(handleOrder) {
            HandleOrder.FIRST -> EventPriority.LOWEST
            HandleOrder.EARLY -> EventPriority.LOW
            HandleOrder.NORMAL -> EventPriority.NORMAL
            HandleOrder.LATE -> EventPriority.HIGH
            HandleOrder.LAST -> EventPriority.HIGHEST
            HandleOrder.MONITOR -> EventPriority.MONITOR
        }
    }
}