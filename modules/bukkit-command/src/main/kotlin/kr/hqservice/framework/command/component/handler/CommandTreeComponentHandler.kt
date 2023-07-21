package kr.hqservice.framework.command.component.handler

import kr.hqservice.framework.command.component.HQCommandRoot
import kr.hqservice.framework.command.component.HQCommandTree
import kr.hqservice.framework.command.component.ParentCommand
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler
import kotlin.reflect.full.findAnnotation

@ComponentHandler(depends = [KoinModuleComponentHandler::class])
class CommandTreeComponentHandler(private val registry: CommandRegistry) :
    HQComponentHandler<HQCommandTree> {
    override fun setup(element: HQCommandTree) {
        if (element is HQCommandRoot) {
            return
        }

        val parentCommand = element::class.findAnnotation<ParentCommand>()
            ?: throw IllegalStateException("Command trees must have parent tree command(s)")
        parentCommand.binds.forEach { parentTree ->
            registry.addTree(parentTree, element)
        }
    }

    override fun teardown(element: HQCommandTree) {
        if (element is HQCommandRoot) {
            return
        }
    }
}