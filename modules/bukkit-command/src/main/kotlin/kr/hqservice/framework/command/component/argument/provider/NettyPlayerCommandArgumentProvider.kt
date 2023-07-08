package kr.hqservice.framework.command.component.argument.provider

import kr.hqservice.framework.command.component.CommandContext
import kr.hqservice.framework.command.component.HQSuspendCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.NettyServer
import org.bukkit.Location

@Component
class NettyPlayerCommandArgumentProvider(
    private val nettyServer: NettyServer
) : HQSuspendCommandArgumentProvider<NettyPlayer> {
    override suspend fun getResult(context: CommandContext, string: String?): Boolean {
        if (string == null) {
            return false
        }
        return nettyServer.getPlayer(string) != null
    }

    override suspend fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String? {
        return "${argumentLabel ?: "플레이어"}를(을) 찾을 수 없습니다."
    }

    override suspend fun cast(context: CommandContext, string: String): NettyPlayer {
        return nettyServer.getPlayer(string)!!
    }

    override suspend fun getTabComplete(
        context: CommandContext,
        location: Location?,
        argumentLabel: String?
    ): List<String> {
        return nettyServer.getPlayers().map { it.getName() }
    }
}