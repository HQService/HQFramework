package kr.hqservice.framework.bukkit.scheduler.command

import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.quartz.CronExpression

@Component
class CronExpressionCommandArgumentProvider : HQCommandArgumentProvider<CronExpression> {
    override fun getResult(context: CommandContext, string: String?): Boolean {
        if (string == null) {
            return false
        }
        return CronExpression.isValidExpression(string.replace("_", " "))
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String {
        if (string == null) {
            return "&Please write cron expression."
        }
        return "&cIllegal cron expression format."
    }

    override fun cast(context: CommandContext, string: String): CronExpression {
        return CronExpression(string.replace("_", " "))
    }

    override fun getTabComplete(context: CommandContext, location: Location?, argumentLabel: String?): List<String> {
        return listOf("CronExpression")
    }
}