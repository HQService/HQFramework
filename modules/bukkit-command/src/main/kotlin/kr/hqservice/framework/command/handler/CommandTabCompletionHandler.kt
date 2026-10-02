package kr.hqservice.framework.command.handler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.command.handler.wrapper.TabCompleteEventWrapper
import kr.hqservice.framework.command.registry.TabCompleteRateLimitRegistry
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@Bean
class CommandTabCompletionHandler(
    private val tabCompleteRateLimitRegistry: TabCompleteRateLimitRegistry,
) : KoinComponent {
    companion object {
        private val handlerMap = ConcurrentHashMap<String, CommandAnnotationHandler.HQBukkitCommand>()
        internal fun findHQCommand(command: String): CommandAnnotationHandler.HQBukkitCommand? {
            return handlerMap[command] ?: handlerMap.values.firstOrNull { it.aliases.contains(command) }
        }
    }
    private val initialized = AtomicBoolean(false)

    internal fun registerTabCompletion(label: String, command: CommandAnnotationHandler.HQBukkitCommand) {
        handlerMap[label] = command
    }

    internal fun unregisterTabCompletion(command: CommandAnnotationHandler.HQBukkitCommand) {
        handlerMap.values.removeIf { it === command }
    }

    fun initialize() {
        if (!initialized.compareAndSet(false, true)) return

        val frameworkPlugin = get<HQBukkitPlugin>(named("hqframework"))
        frameworkPlugin.server.pluginManager.apply {
            val eventClass = Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent") as Class<Event>
            val eventWrapper = TabCompleteEventWrapper(tabCompleteRateLimitRegistry, eventClass.kotlin)
            registerEvent(eventClass, object : Listener {}, EventPriority.LOWEST, { _, event ->
                eventWrapper.execute(event)
            }, frameworkPlugin, true)
        }
    }
}
