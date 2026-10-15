package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.registry.CommandArgumentExceptionHandlerRegistry
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.command.CommandSender
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf

@Bean
class CommandArgumentExceptionHandlerRegistryImpl : CommandArgumentExceptionHandlerRegistry {
    private class Registration(
        val throwableType: KClass<*>,
        val senderType: KClass<*>,
        val instance: CommandArgumentExceptionHandler<Throwable, CommandSender>
    )

    private val exceptionHandlers = CopyOnWriteArrayList<Registration>()

    override fun register(
        throwableType: KClass<*>,
        senderType: KClass<*>,
        instance: CommandArgumentExceptionHandler<Throwable, CommandSender>
    ) {
        exceptionHandlers.removeIf { it.throwableType == throwableType && it.senderType == senderType }
        exceptionHandlers.add(Registration(throwableType, senderType, instance))
    }

    override fun find(throwableType: KClass<*>, senderType: KClass<*>): CommandArgumentExceptionHandler<Throwable, CommandSender>? {
        return exceptionHandlers
            .filter { it.throwableType.isSuperclassOf(throwableType) && it.senderType.isSuperclassOf(senderType) }
            .maxWithOrNull(compareBy({ it.throwableType.allSuperclasses.size }, { it.senderType.allSuperclasses.size }))
            ?.instance
    }
}
