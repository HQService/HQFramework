package kr.hqservice.framework.bukkit.core.listener

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
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

    @OptIn(ExperimentalStdlibApi::class)
    private fun invokeHandlerMethod(event: Event) {
        try {
            if (suspend) {
                if (event::class.simpleName == "PlayerRepositoryLoadedEvent") runBlocking {
                    method.callSuspend(listenerInstance, event)
                } else runBlocking {
                    val dispatcher = coroutineContext[CoroutineDispatcher.Key]!!
                    CoroutineScope(plugin.coroutineContext.minusKey(CoroutineDispatcher.Key)).launch(dispatcher) {
                        method.callSuspend(listenerInstance, event)
                    }
                }
            } else method.call(listenerInstance, event)
        } catch (exception: InvocationTargetException) {
            val cause = exception.cause ?: exception
            throw cause
        }
    }
}