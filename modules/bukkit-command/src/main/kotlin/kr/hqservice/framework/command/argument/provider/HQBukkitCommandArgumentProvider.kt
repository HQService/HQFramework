package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Server

@Component
class HQBukkitCommandArgumentProvider(private val server: Server) : CommandArgumentProvider<HQBukkitPlugin> {
    override suspend fun cast(context: CommandContext, argument: String?): HQBukkitPlugin {
        argument ?: throw ArgumentFeedback.RequireArgument
        return server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>().firstOrNull {
            it.name.lowercase() == argument.lowercase()
        } ?: throw ArgumentFeedback.Message("&c$argument 의 이름을 가진 HQPlugin 이 존재하지 않습니다.")
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>().map {
            it.name
        }
    }
}