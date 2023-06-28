package kr.hqservice.framework.bukkit.core.extension

import org.bukkit.command.CommandSender

fun CommandSender.sendColorizedMessage(string: String) {
    this.sendMessage(string.colorize())
}

fun CommandSender.sendMessages(vararg messages: String?) {
    messages.filterNotNull().forEach(::sendColorizedMessage)
}