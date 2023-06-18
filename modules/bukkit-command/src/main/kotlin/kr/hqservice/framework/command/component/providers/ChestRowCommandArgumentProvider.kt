package kr.hqservice.framework.command.component.providers

import kr.hqservice.framework.command.component.CommandContext
import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location

@Component
class ChestRowCommandArgumentProvider : HQCommandArgumentProvider<ChestRow> {
    override fun getResult(context: CommandContext, string: String?): Boolean {
        if (string == null) {
            return false
        }
        val int = string.toIntOrNull() ?: return false
        return !(int < 1 || int > 6)
    }

    override fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String?): String? {
        return "1~6 사이의 수를 입력해주세요."
    }

    override fun cast(context: CommandContext, string: String): ChestRow {
        return object : ChestRow {
            override val row: Int = string.toInt()
        }
    }

    override fun getTabComplete(
        context: CommandContext,
        location: Location?,
        argumentLabel: String?
    ): List<String> {
        return listOf("1", "2", "3", "4", "5", "6")
    }
}

interface ChestRow {
    val row: Int
}