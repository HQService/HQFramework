package kr.hqservice.framework.command.component.handler

import kr.hqservice.framework.command.component.HQCommandRoot
import kr.hqservice.framework.command.component.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler
import org.bukkit.command.CommandMap
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.SimplePluginManager
import java.util.logging.Logger
import kotlin.reflect.full.declaredMemberProperties

@ComponentHandler(
    depends = [
        KoinModuleComponentHandler::class,
        CommandNodeComponentHandler::class,
        CommandTreeComponentHandler::class,
        CommandArgumentProviderComponentHandler::class,
    ]
)
class CommandRootComponentHandler(
    private val pluginManager: PluginManager,
    private val logger: Logger,
    private val registry: CommandRegistry,
    private val argumentProviderRepository: CommandArgumentProviderRegistry
) : HQComponentHandler<HQCommandRoot> {
    override fun setup(element: HQCommandRoot) {
        element.setup(registry)
        if (pluginManager is SimplePluginManager) {
            element.register(getCommandMap(pluginManager), argumentProviderRepository)
        } else {
            logger.info("skipping registration while mocking")
        }
    }

    private companion object {
        private fun getCommandMap(pluginManager: SimplePluginManager): CommandMap {
            return SimplePluginManager::class
                .declaredMemberProperties
                .first { it.name == "commandMap" }
                .get(pluginManager) as CommandMap
        }
    }
}