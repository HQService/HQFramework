package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location

@Component
class DoubleCommandArgumentProvider : HQCommandArgumentProvider<Double> {
    override fun getTabComplete(
        context: CommandContext,
        location: Location?,
        argumentLabel: String?
    ): List<String> {
        return listOf(argumentLabel ?: "숫자")
    }

    override fun getResult(context: CommandContext, string: String?): Boolean {
        return string?.toDoubleOrNull() != null
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String? {
        return "${argumentLabel ?: "숫자"}을(를) 입력해야 합니다."
    }

    override fun cast(context: CommandContext, string: String): Double {
        return string.toDouble()
    }
}