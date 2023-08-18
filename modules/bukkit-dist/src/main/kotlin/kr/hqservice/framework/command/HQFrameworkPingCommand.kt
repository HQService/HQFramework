package kr.hqservice.framework.command

import org.bukkit.command.ConsoleCommandSender

@Command(label = "hqp")
class HQFrameworkPingCommand {
    @CommandExecutor("ping")
    fun ping(consoleCommandSender: ConsoleCommandSender) {
        consoleCommandSender.sendMessage("pong!")
    }
}