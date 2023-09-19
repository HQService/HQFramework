package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player

@Component
class PlayerCommandArgumentProvider(private val server: Server) : CommandArgumentProvider<Player> {
    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return server.onlinePlayers.map {
            if (it.displayName.equals(it.name, true)) it.name
            else it.displayName
        }
    }

    override suspend fun cast(context: CommandContext, argument: String?): Player {
        argument ?: throw ArgumentFeedback.RequireArgument
        return server.onlinePlayers.firstOrNull { it.displayName == argument || it.name == argument } ?: throw ArgumentFeedback.PlayerNotFound
    }
}