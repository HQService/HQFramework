package kr.hqservice.framework.command.component.providers

import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.command.CommandSender
import javax.xml.stream.Location

@Component
class StringCommandArgumentProvider : HQCommandArgumentProvider<String> {
    override fun getTabComplete(commandSender: CommandSender, location: Location, argumentLabel: String?): List<String> {
        return listOf(argumentLabel ?: "문자열")
    }

    override fun getResult(commandSender: CommandSender, string: String?): Boolean {
        return true
    }

    override fun getFailureMessage(commandSender: CommandSender, string: String?, argumentLabel: String?): String? {
        return null
    }

    override fun cast(string: String): String {
        return string
    }
}