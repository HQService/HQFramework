package kr.hqservice.framework.command.handler.wrapper


import kr.hqservice.framework.command.handler.CommandTabCompletionHandler.Companion.findHQCommand
import kr.hqservice.framework.command.registry.TabCompleteRateLimitRegistry
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

class TabCompleteEventWrapper(
    private val tabCompleteRateLimitRegistry: TabCompleteRateLimitRegistry,
    clazz: KClass<Event>
) {
    private val buffer = clazz.memberFunctions.single { it.name == "getBuffer" }
    private val isCommand = clazz.memberFunctions.single { it.name == "isCommand" }
    private val sender = clazz.memberFunctions.single { it.name == "getSender" }
    private val location = clazz.memberFunctions.single { it.name == "getLocation" }
    private val setCompletions = clazz.memberFunctions.single { it.name == "setCompletions" }
    private val setHandled = clazz.memberFunctions.single { it.name == "setHandled" }
    private val isHandled = clazz.memberFunctions.single { it.name == "isHandled" }

    internal fun execute(event: Any) {
        if (isHandled.call(event) as Boolean) return

        val buffer = buffer.call(event) as String
        val isCommand = isCommand.call(event) as Boolean
        if ((!isCommand && !buffer.startsWith("/")) || buffer.indexOf(' ') == -1)
            return

        val args = buffer.split(" ")
        val command = args[0].removePrefix("/")
        val sender = sender.call(event) as CommandSender

        val location = location.call(event) as? Location
        val completion = if(sender is Player && !requestTabComplete(sender.uniqueId)) null else findHQCommand(command)?.hqTabComplete(
            sender, command,
            if (args.isNotEmpty()) args.subList(1, args.size).toTypedArray() else arrayOf(""), location
        )

        completion?.apply {
            setCompletions.call(event, this)
            setHandled.call(event, true)
        }
    }

    private fun requestTabComplete(playerUniqueId: UUID) = tabCompleteRateLimitRegistry.requestTabComplete(playerUniqueId)
}