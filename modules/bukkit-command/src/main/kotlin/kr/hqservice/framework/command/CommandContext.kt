package kr.hqservice.framework.command

import org.bukkit.command.CommandSender

interface CommandContext {
    fun getCommandSender(): CommandSender

    fun getArgumentLabel(): String

    fun findArgumentByIndex(index: Int): String

    fun findArgument(key: String): String?

    fun getArgument(key: String): String

    fun getArguments(): Collection<String>
}