package kr.hqservice.framework.command.argument.provider

import kr.hqservice.framework.bukkit.core.extension.parseLocalDateTimeOrNull
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import java.time.LocalDateTime

@Component
class LocalDateTimeCommandArgumentProvider : HQCommandArgumentProvider<LocalDateTime> {
    override fun getResult(context: CommandContext, string: String?): Boolean {
        if (string == null) {
            return false
        }

        return string.replace("_", " ").parseLocalDateTimeOrNull() != null
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String? {
        val label = argumentLabel ?: "DateTime"
        if (string == null) {
            return "${label}을(를) 입력해주새요."
        }
        return "${label}을(를) 올바르게 입력해주세요."
    }

    override fun cast(context: CommandContext, string: String): LocalDateTime {
        return string.replace("_", " ").parseLocalDateTimeOrNull()!!
    }

    override fun getTabComplete(context: CommandContext, location: Location?, argumentLabel: String?): List<String> {
        return listOf("yyyy-MM-dd_HH:mm:ss")
    }
}