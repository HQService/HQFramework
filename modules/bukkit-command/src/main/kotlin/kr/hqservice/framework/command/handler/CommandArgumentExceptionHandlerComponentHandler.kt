package kr.hqservice.framework.command.handler

import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.registry.CommandArgumentExceptionHandlerRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.extension.print
import org.bukkit.command.CommandSender
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

@ComponentHandler
class CommandArgumentExceptionHandlerComponentHandler(
    private val registry: CommandArgumentExceptionHandlerRegistry
) : HQComponentHandler<CommandArgumentExceptionHandler<*, *>> {
    override fun setup(element: CommandArgumentExceptionHandler<*, *>) {
        element::class.simpleName.print("simpleName: ")
        println("th")
        val throwable = element::class.supertypes
            .first { it.isSubtypeOf(CommandArgumentExceptionHandler::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure

        val sender = element::class.supertypes
            .first { it.isSubtypeOf(CommandArgumentExceptionHandler::class.starProjectedType) }
            .arguments
            .last()
            .type!!.jvmErasure

        @Suppress("UNCHECKED_CAST")
        element as CommandArgumentExceptionHandler<Throwable, CommandSender>

        registry.register(throwable, sender, element)
    }
}