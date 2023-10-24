package kr.hqservice.framework.command

import org.bukkit.command.CommandSender

interface CommandSuggestible {
    val priority: Int
    val label: String
    val aliases: List<String>
    val permission: String
    val isOp: Boolean
    val hideSuggestion: Boolean

    // validateSuggestion
    fun validateSuggestion(sender: CommandSender, isHide: Boolean = false): Boolean {
        if (isHide && hideSuggestion) {
            return false
        }
        if (sender.isOp || sender.hasPermission(permission)) {
            return true
        }
        if (!sender.isOp && isOp) {
            return false
        }
        if (permission != "" && !sender.hasPermission(permission)) {
            return false
        }
        return true
    }
}