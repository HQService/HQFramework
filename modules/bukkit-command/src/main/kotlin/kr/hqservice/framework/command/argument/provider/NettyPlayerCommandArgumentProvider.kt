package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.NettyServer
import org.bukkit.Location

@Component
class NettyPlayerCommandArgumentProvider(
    private val nettyServer: NettyServer
) : CommandArgumentProvider<NettyPlayer> {
    override suspend fun cast(context: CommandContext, argument: String?): NettyPlayer {
        argument ?: throw ArgumentFeedback.RequireArgument
        return nettyServer.getPlayer(argument) ?: throw ArgumentFeedback.PlayerNotFound
    }

    override suspend fun getTabComplete(
        context: CommandContext,
        location: Location?
    ): List<String> {
        return nettyServer.getPlayers().map { it.getName() }
    }
}