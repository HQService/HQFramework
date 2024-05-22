package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import org.bukkit.Location

@Qualifier("chestRow")
@Component
class ChestRowCommandArgumentProvider : CommandArgumentProvider<Int> {

    private val tabs = (1..6).map(Int::toString)

    override suspend fun cast(context: CommandContext, argument: String?): Int {
        argument ?: throw ArgumentFeedback.RequireArgument
        val int = argument.toIntOrNull() ?: throw ArgumentFeedback.NotNumber
        if (int < 1 || int > 6) {
            throw ArgumentFeedback.Message("&c1과 6 사이의 수를 입력해 주세요.")
        }
        return int
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return tabs
    }
}