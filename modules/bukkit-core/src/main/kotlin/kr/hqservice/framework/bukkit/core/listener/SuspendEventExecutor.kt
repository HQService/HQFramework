package kr.hqservice.framework.bukkit.core.listener

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

/**
 * @param eventClass event handler 의 첫번째 인자
 */
class SuspendEventExecutor(
    private val eventClass: KClass<*>,
    private val listenerInstance: Any,
    private val method: KFunction<*>,
    private val plugin: HQBukkitPlugin
) : EventExecutor {
    override fun execute(empty: Listener, event: Event) {
        if (eventClass.isInstance(event)) {
            val dispatcher = if (event.isAsynchronous) {
                Dispatchers.BukkitAsync
            } else {
                Dispatchers.BukkitMain
            }

            plugin.launch(dispatcher, CoroutineStart.UNDISPATCHED) {
                try {
                    if (method.isSuspend) {
                        method.callSuspend(listenerInstance, event)
                    } else {
                        method.call(listenerInstance, event)
                    }
                } catch (exception: InvocationTargetException) {
                    val cause = exception.cause ?: exception
                    throw cause
                }
            }
        }
    }
}