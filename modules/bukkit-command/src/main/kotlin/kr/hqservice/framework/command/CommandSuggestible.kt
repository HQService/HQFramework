package kr.hqservice.framework.command

import org.bukkit.command.CommandSender

interface CommandSuggestible {
    val priority: Int
    val label: String
    val permission: String
    val isOp: Boolean
    val hideSuggestion: Boolean

    // validateSuggestion
    fun hasPermission(sender: CommandSender): Boolean {
        if (sender.isOp || sender.hasPermission(permission)) {
            return true
        }
        if (!sender.isOp && isOp) {
            return false
        }
        if (permission != "" && !sender.hasPermission(permission)) {
            return false
        }
        if (hideSuggestion) {
            return false
        }
        return true
    }
}