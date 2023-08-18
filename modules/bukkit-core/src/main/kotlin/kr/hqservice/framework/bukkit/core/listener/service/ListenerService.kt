package kr.hqservice.framework.bukkit.core.listener.service

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.listener.SuspendEventExecutor
import kr.hqservice.framework.bukkit.core.listener.exception.ListenerRegistrationFailedException
import kr.hqservice.framework.global.core.component.Service
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.MethodCall
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.RegisteredListener
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

@Service
class ListenerService {
    fun createListener(instance: Any, plugin: HQBukkitPlugin): Map<KClass<*>, MutableCollection<RegisteredListener>> {
        val listener = createRedefinedListener(instance, plugin)
        return listener::class.declaredFunctions
            .filter {
                it.hasAnnotation<Subscribe>()
            }.filter {
                it.javaMethod?.isBridge != true && it.javaMethod?.isSynthetic != true
            }.onEach {
                if (!it.isAccessible) {
                    it.isAccessible = true
                }
            }.groupBy {
                val firstParameter = it.valueParameters.firstOrNull()
                if (firstParameter?.type?.isSubtypeOf(Event::class.starProjectedType) == false) {
                    throw ListenerRegistrationFailedException(instance::class, "first parameter of event handler must be event.")
                }
                firstParameter!!.type.jvmErasure
            }.mapValues { (event, kFunctions) ->
                kFunctions.map { kFunction ->
                    val executor = SuspendEventExecutor(event, kFunction, plugin)
                    val annotation = kFunction.findAnnotation<Subscribe>()!!
                    RegisteredListener(listener, executor, getBukkitEventPriority(annotation.handleOrder), plugin, annotation.ignoreCancelled)
                }.toMutableList()
            }
    }

    private fun createRedefinedListener(instance: Any, plugin: HQBukkitPlugin): Listener {
        return if (instance !is Listener) {
            val redefined = ByteBuddy()
                .redefine(instance::class.java)
                .name(instance::class.qualifiedName!! + "\$handlerProxy")
                .implement(Listener::class.java)
                .run {
                    try {
                        instance::class.java.getConstructor()
                        this
                    } catch (exception: NoSuchMethodException) {
                        this
                            .defineConstructor(Visibility.PUBLIC)
                            .intercept(MethodCall.invokeSuper())
                    }
                }
                .make()
                .load(plugin.getPluginClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .loaded
            val redefinedInstance: Listener = redefined.getConstructor().newInstance() as Listener
            redefinedInstance.apply {
                copyFields(instance, redefinedInstance)
            }
        } else {
            instance
        }
    }

    private fun copyFields(source: Any, destination: Any) {
        for (sourceField in source::class.java.getDeclaredFields()) {
            val destinationField = destination::class.java.getDeclaredField(sourceField.name)
            sourceField.setAccessible(true)
            destinationField.setAccessible(true)
            val valueToCopy = sourceField[source]
            destinationField.set(destination, valueToCopy)
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