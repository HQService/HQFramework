package kr.hqservice.framework.command.component.providers

import kr.hqservice.framework.command.component.CommandContext
import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location

@Component
class StringCommandArgumentProvider : HQCommandArgumentProvider<String> {
    override fun getTabComplete(context: CommandContext, location: Location?, argumentLabel: String?): List<String> {
        return listOf(argumentLabel ?: "문자열")
    }

    override fun getResult(context: CommandContext, string: String?): Boolean {
        return true
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String? {
        return "${argumentLabel ?: "문자열"}을(를) 입력해야 합니다."
    }

    override fun cast(context: CommandContext, string: String): String {
        return string
    }
}