package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.bukkit.core.extension.parseLocalDateTimeOrNull
import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import java.time.LocalDateTime

@Component
class LocalDateTimeCommandArgumentProvider : CommandArgumentProvider<LocalDateTime> {
    override suspend fun cast(context: CommandContext, argument: String?): LocalDateTime {
        argument ?: throw ArgumentFeedback.RequireArgument
        return argument.replace("_", " ").parseLocalDateTimeOrNull()
            ?: throw ArgumentFeedback.Message("&c${context.getArgumentLabel()} 값이 올바르지 않습니다.")
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return listOf("yyyy-MM-dd_HH:mm:ss")
    }
}