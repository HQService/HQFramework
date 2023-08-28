package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location

@Component
class DoubleCommandArgumentProvider : CommandArgumentProvider<Double> {
    override suspend fun cast(context: CommandContext, argument: String?): Double {
        argument ?: throw ArgumentFeedback.RequireArgument
        return argument.toDouble()
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return listOf("숫자")
    }
}