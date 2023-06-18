package kr.hqservice.framework.command.providers

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.command.component.CommandContext
import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.command.CommandSender

@Component
class HQBukkitPluginCommandArgumentProvider(private val server: Server) : HQCommandArgumentProvider<HQBukkitPlugin> {
    override fun getTabComplete(
        context: CommandContext,
        location: Location?,
        argumentLabel: String?
    ): List<String> {
        return server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>().map {
            it.name
        }
    }

    override fun getResult(context: CommandContext, string: String?): Boolean {
        return server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>().any {
            it.name.lowercase() == string?.lowercase()
        }
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String {
        return if (string == null) {
            "&cHQPlugin 이름을 입력해주세요.".colorize()
        } else {
            "&c$string 의 이름을 가진 HQPlugin 이 존재하지 않습니다.".colorize()
        }
    }

    override fun cast(context: CommandContext, string: String): HQBukkitPlugin {
        return server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>().first { it.name.lowercase() == string.lowercase() }
    }
}