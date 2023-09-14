package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Material

@Component
class MaterialCommandArgumentProvider : CommandArgumentProvider<Material> {
    override suspend fun cast(context: CommandContext, argument: String?): Material {
        argument ?: throw ArgumentFeedback.RequireArgument
        return Material.matchMaterial(argument) ?: throw ArgumentFeedback.Message("존재하지 않는 타입입니다.")
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return Material.values().map { it.name }
    }
}