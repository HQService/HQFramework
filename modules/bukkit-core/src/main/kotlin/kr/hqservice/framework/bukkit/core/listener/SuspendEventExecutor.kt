package kr.hqservice.framework.bukkit.core.listener

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.hasAnnotation

/**
 * @param eventClass event handler 의 첫번째 인자
 */
class SuspendEventExecutor(
    private val eventClass: KClass<*>,
    private val listenerInstance: Any,
    private val method: KFunction<*>,
    private val plugin: HQBukkitPlugin
) : EventExecutor {
    @OptIn(ExperimentalStdlibApi::class)
    override fun execute(empty: Listener, event: Event) {
        if (eventClass.isInstance(event)) {
            runBlocking {
                val dispatcher = coroutineContext[CoroutineDispatcher.Key]!!
                CoroutineScope(plugin.coroutineContext.minusKey(CoroutineDispatcher.Key)).launch(dispatcher) {
                    invokeHandlerMethod(event)
                }
            }
        }
    }

    private suspend fun invokeHandlerMethod(event: Event) {
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