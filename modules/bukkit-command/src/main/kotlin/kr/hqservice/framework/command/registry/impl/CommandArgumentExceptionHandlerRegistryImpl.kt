package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.registry.CommandArgumentExceptionHandlerRegistry
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.command.CommandSender
import kotlin.reflect.KClass

@Bean
class CommandArgumentExceptionHandlerRegistryImpl : CommandArgumentExceptionHandlerRegistry {
    private val exceptionHandlers: MutableMap<String, CommandArgumentExceptionHandler<Throwable, CommandSender>> = mutableMapOf()

    override fun register(
        throwableType: KClass<*>,
        senderType: KClass<*>,
        instance: CommandArgumentExceptionHandler<Throwable, CommandSender>
    ) {
        exceptionHandlers[getExceptionHandlerKey(throwableType, senderType)] = instance
    }

    override fun find(
        throwableType: KClass<*>,
        senderType: KClass<*>
    ): CommandArgumentExceptionHandler<Throwable, CommandSender>? {
        return exceptionHandlers[getExceptionHandlerKey(throwableType, senderType)]
            ?: exceptionHandlers[getExceptionHandlerKey(throwableType, CommandSender::class)]
    }

    private fun getExceptionHandlerKey(throwableType: KClass<*>, senderType: KClass<*>): String {
        return "${throwableType.qualifiedName}_\$_${senderType.qualifiedName}"
    }
}