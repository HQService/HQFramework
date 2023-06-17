package kr.hqservice.framework.command.component.handler

import kr.hqservice.framework.command.component.CommandArgumentProvider
import kr.hqservice.framework.command.component.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler

@ComponentHandler(depends = [KoinModuleComponentHandler::class])
class CommandArgumentProviderComponentHandler(private val registry: CommandArgumentProviderRegistry) : HQComponentHandler<CommandArgumentProvider<*>> {
    override fun setup(element: CommandArgumentProvider<*>) {
        registry.addProvider(element)
    }
}