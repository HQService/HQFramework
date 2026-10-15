package kr.hqservice.framework.command.handler

import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.SimplePluginManager
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

internal object CommandMapAccess {
    private val logger = Logger.getLogger(CommandMapAccess::class.java.name)
    private val reportedFailures = ConcurrentHashMap.newKeySet<String>()

    fun commandMap(server: Server): CommandMap? {
        return try {
            server.commandMap
        } catch (_: NoSuchMethodError) {
            reflect("SimplePluginManager.commandMap") {
                SimplePluginManager::class.java.getDeclaredField("commandMap")
                    .apply { isAccessible = true }
                    .get(server.pluginManager) as CommandMap
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun knownCommands(commandMap: CommandMap): MutableMap<String, Command>? {
        return try {
            commandMap.knownCommands
        } catch (_: NoSuchMethodError) {
            reflect("SimpleCommandMap.knownCommands") {
                SimpleCommandMap::class.java.getDeclaredField("knownCommands")
                    .apply { isAccessible = true }
                    .get(commandMap) as MutableMap<String, Command>
            }
        }
    }

    private fun <T> reflect(target: String, access: () -> T): T? {
        return runCatching(access)
            .onFailure { if (reportedFailures.add(target)) logger.log(Level.WARNING, "cannot access $target", it) }
            .getOrNull()
    }
}
