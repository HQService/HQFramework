package kr.hqservice.framework.database.redis.handler

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.database.redis.RedisChannel
import kr.hqservice.framework.database.redis.RedisMessenger
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.redis.RedisSubscriber
import kr.hqservice.framework.database.redis.Subscription
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import java.lang.reflect.InvocationTargetException
import java.util.logging.Logger
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

@AnnotationHandler
class RedisSubscriberAnnotationHandler(
    private val messenger: RedisMessenger,
    private val settings: RedisSettings,
    private val json: Json,
    private val plugin: HQBukkitPlugin,
    private val logger: Logger,
) : HQAnnotationHandler<RedisSubscriber> {
    private val subscriptions: MutableList<Pair<Any, Subscription>> = mutableListOf()

    override fun setup(instance: Any, annotation: RedisSubscriber) {
        val channels = instance::class.declaredMemberFunctions
            .mapNotNull { function -> function.findAnnotation<RedisChannel>()?.let { function to it.value } }
            .map { (function, channel) -> Triple(function, channel, serializerOf(instance, function)) }
        if (!settings.enabled) {
            logger.warning("@RedisSubscriber ${instance::class.simpleName} is ignored because redis.uri is not configured")
            return
        }
        channels.forEach { (function, channel, serializer) ->
            function.isAccessible = true
            val subscription = messenger.subscribe(channel, serializer, plugin) { value -> invoke(function, instance, value) }
            subscriptions += instance to subscription
        }
    }

    override fun teardown(instance: Any, annotation: RedisSubscriber) {
        subscriptions.filter { it.first === instance }.forEach { (_, subscription) -> subscription.close() }
        subscriptions.removeIf { it.first === instance }
    }

    private fun serializerOf(instance: Any, function: KFunction<*>): KSerializer<Any?> {
        val parameter = function.valueParameters.singleOrNull()
            ?: throw IllegalStateException("@RedisChannel function ${instance::class.simpleName}.${function.name} must take exactly one parameter: the message")
        return json.serializersModule.serializer(parameter.type)
    }

    private suspend fun invoke(function: KFunction<*>, instance: Any, value: Any?) {
        try {
            if (function.isSuspend) function.callSuspend(instance, value) else function.call(instance, value)
        } catch (exception: InvocationTargetException) {
            throw exception.cause ?: exception
        }
    }
}
