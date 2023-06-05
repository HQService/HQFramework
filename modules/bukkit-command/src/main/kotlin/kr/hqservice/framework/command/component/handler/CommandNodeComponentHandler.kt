package kr.hqservice.framework.command.component.handler

import kr.hqservice.framework.command.component.CommandExecutor
import kr.hqservice.framework.command.component.Depend
import kr.hqservice.framework.command.component.HQCommandNode
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

@ComponentHandler(depends = [KoinModuleComponentHandler::class])
class CommandNodeComponentHandler(private val registry: CommandRegistry) :
    HQComponentHandler<HQCommandNode> {
    override fun setup(element: HQCommandNode) {
        val depend = element::class.findAnnotation<Depend>() ?: throw IllegalStateException("Command nodes must have parent tree command(s)")

        element::class.memberFunctions.filterIsInstance<KFunction<Unit>>().forEach { function ->
            function.annotations.filterIsInstance<CommandExecutor>().forEach { commandExecutor ->
                element.addExecutor(commandExecutor, function)
            }
        }

        depend.depends.forEach { parentCommand ->
            registry.addNode(parentCommand, element)
        }
    }
}