package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location

@Component
class BooleanCommandArgumentProvider : CommandArgumentProvider<Boolean> {

    private val tabs = listOf("true", "false")

    override suspend fun cast(context: CommandContext, argument: String?): Boolean {
        argument ?: throw ArgumentFeedback.RequireArgument
        return argument.toBooleanStrictOrNull() ?: throw ArgumentFeedback.NotBoolean
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return tabs
    }
}