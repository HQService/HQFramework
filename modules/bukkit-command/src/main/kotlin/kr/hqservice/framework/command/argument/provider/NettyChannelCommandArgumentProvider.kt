package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyServer
import org.bukkit.Location

@Component
class NettyChannelCommandArgumentProvider(
    private val nettyServer: NettyServer
) : CommandArgumentProvider<NettyChannel> {
    override suspend fun cast(context: CommandContext, argument: String?): NettyChannel {
        argument ?: throw ArgumentFeedback.RequireArgument
        return nettyServer.getChannel(argument) ?: throw ArgumentFeedback.Message("§c존재하지 않는 채널입니다.")
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return nettyServer.getChannels().map { it.getName() }
    }
}