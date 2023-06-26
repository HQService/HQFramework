package kr.hqservice.framework.command.component.handler

import kr.hqservice.framework.command.component.HQCommandNode
import kr.hqservice.framework.command.component.ParentCommand
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler
import kotlin.reflect.full.findAnnotation

@ComponentHandler(depends = [KoinModuleComponentHandler::class])
class CommandNodeComponentHandler(
    private val registry: CommandRegistry,
) : HQComponentHandler<HQCommandNode> {
    override fun setup(element: HQCommandNode) {
        val parentCommand = element::class.findAnnotation<ParentCommand>() ?: throw IllegalStateException("Command nodes must have parent tree command(s)")

        parentCommand.binds.forEach { parentTree ->
            registry.addNode(parentTree, element)
        }
    }
}