package kr.hqservice.framework.command.handler

import kr.hqservice.framework.command.handler.wrapper.TabCompleteEventWrapper
import kr.hqservice.framework.command.registry.TabCompleteRateLimitRegistry
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

@Bean
class CommandTabCompletionHandler(
    private val tabCompleteRateLimitRegistry: TabCompleteRateLimitRegistry,
) {
    companion object {
        private val handlerMap = mutableMapOf<String, CommandAnnotationHandler.HQBukkitCommand>()
        internal fun findHQCommand(command: String): CommandAnnotationHandler.HQBukkitCommand? {
            return handlerMap[command]
        }
    }
    private var initialized = false

    internal fun registerTabCompletion(label: String, command: CommandAnnotationHandler.HQBukkitCommand) {
        handlerMap[label] = command
    }

    fun initialize(plugin: Plugin) {
        if (initialized) return
        initialized = true

        plugin.server.pluginManager.apply {
            val eventClass = Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent") as Class<Event>
            val eventWrapper = TabCompleteEventWrapper(tabCompleteRateLimitRegistry, eventClass.kotlin)
            registerEvent(eventClass, object : Listener {}, EventPriority.LOWEST, { _, event ->
                eventWrapper.execute(event)
            }, plugin, true)
        }
    }
}