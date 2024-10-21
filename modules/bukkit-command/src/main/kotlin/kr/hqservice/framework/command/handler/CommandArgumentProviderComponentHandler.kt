package kr.hqservice.framework.command.handler

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kotlin.reflect.full.findAnnotation

@ComponentHandler
class CommandArgumentProviderComponentHandler(
    private val registry: CommandArgumentProviderRegistry
) : HQComponentHandler<CommandArgumentProvider<*>> {
    override fun setup(element: CommandArgumentProvider<*>) {
        registry.addProvider(element, element::class.findAnnotation<Qualifier>()?.value)
    }
}