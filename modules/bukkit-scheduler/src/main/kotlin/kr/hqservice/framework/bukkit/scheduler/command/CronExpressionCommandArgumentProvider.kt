package kr.hqservice.framework.bukkit.scheduler.command

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.quartz.CronExpression

@Component
class CronExpressionCommandArgumentProvider : CommandArgumentProvider<CronExpression> {
    override suspend fun cast(context: CommandContext, argument: String?): CronExpression {
        argument ?: throw ArgumentFeedback.RequireArgument
        if (!CronExpression.isValidExpression(argument.replace("_", " "))) {
            throw ArgumentFeedback.Message("&cIllegal cron expression format.")
        }
        return CronExpression(argument.replace("_", " "))
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return listOf("CronExpression")
    }
}