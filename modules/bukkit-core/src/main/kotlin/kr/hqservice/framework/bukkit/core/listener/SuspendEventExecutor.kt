package kr.hqservice.framework.bukkit.core.listener

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
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
    private val suspend = method.isSuspend

    override fun execute(empty: Listener, event: Event) {
        if (eventClass.isInstance(event)) {
            invokeHandlerMethod(event)
        }
    }

    private fun invokeHandlerMethod(event: Event) {
        try {
            if (suspend) {
                plugin.launch(Dispatchers.BukkitMain, start = CoroutineStart.UNDISPATCHED) {
                    method.callSuspend(listenerInstance, event)
                }
            } else method.call(listenerInstance, event)
        } catch (exception: InvocationTargetException) {
            throw exception.cause ?: exception
        }
    }
}