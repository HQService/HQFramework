package kr.hqservice.framework.command.registry

import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import org.bukkit.command.CommandSender
import kotlin.reflect.KClass

interface CommandArgumentExceptionHandlerRegistry {
    fun register(throwableType: KClass<*>, senderType: KClass<*>, instance: CommandArgumentExceptionHandler<Throwable, CommandSender>)

    fun find(throwableType: KClass<*>, senderType: KClass<*>): CommandArgumentExceptionHandler<Throwable, CommandSender>?
}