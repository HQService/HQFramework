package kr.hqservice.framework.command.component.argument.provider

import kr.hqservice.framework.command.component.CommandContext
import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class LocalDateTimeCommandArgumentProvider : HQCommandArgumentProvider<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun getResult(context: CommandContext, string: String?): Boolean {
        if (string == null) {
            return false
        }

        try {
            LocalDateTime.parse(string.replace("_", " "), formatter)
        } catch (exception: DateTimeParseException) {
            return false
        }

        return true
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String? {
        val label = argumentLabel ?: "DateTime"
        if (string == null) {
            return "${label}을(를) 입력해주새요."
        }
        return "${label}을(를) 올바르게 입력해주세요."
    }

    override fun cast(context: CommandContext, string: String): LocalDateTime {
        return LocalDateTime.parse(string.replace("_", " "), formatter)
    }

    override fun getTabComplete(context: CommandContext, location: Location?, argumentLabel: String?): List<String> {
        return listOf("yyyy-MM-dd_HH:mm:ss")
    }
}