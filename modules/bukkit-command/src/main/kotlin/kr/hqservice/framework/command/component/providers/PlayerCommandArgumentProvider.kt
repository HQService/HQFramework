package kr.hqservice.framework.command.component.providers

import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Component
class PlayerCommandArgumentProvider(private val server: Server) : HQCommandArgumentProvider<Player> {
    override fun getTabComplete(
        commandSender: CommandSender,
        location: Location?,
        argumentLabel: String?
    ): List<String> {
        return server.onlinePlayers.map { it.name }
    }

    override fun getResult(commandSender: CommandSender, string: String?): Boolean {
        return string?.let { server.getPlayerExact(it) } != null
    }

    override fun getFailureMessage(commandSender: CommandSender, string: String?, argumentLabel: String?): String? {
        return "접속중인 ${argumentLabel ?: "플레이어"}의 이름을 입력해야 합니다."
    }

    override fun cast(string: String): Player {
        return server.getPlayerExact(string) ?: throw NullPointerException("player $string is null")
    }
}