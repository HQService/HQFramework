package kr.hqservice.framework.command

import org.bukkit.command.CommandSender
import kotlin.reflect.KClassifier

interface CommandContext {
    fun getCommandSender(): CommandSender

    fun findArgument(key: String): String?

    fun getArgument(key: String): String

    fun getArguments(): Collection<String>

    fun getArgumentsByType(kClassifier: KClassifier): Collection<String>
}