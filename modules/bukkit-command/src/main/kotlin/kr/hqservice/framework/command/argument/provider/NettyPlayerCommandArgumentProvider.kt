package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.HQSuspendCommandArgumentProvider
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
        val label = argumentLabel ?: "플레이어"
        if (string == null) {
            return "${label}을(를) 입력해주세요."
        }
        return "${label}을(를) 찾을 수 없습니다."
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