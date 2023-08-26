package kr.hqservice.framework.command

import org.bukkit.command.CommandSender

interface CommandSuggestible {
    val priority: Int
    val label: String
    val permission: String
    val isOp: Boolean
    val hideSuggestion: Boolean

    fun validatePermission(sender: CommandSender): Boolean {
        if (this.hideSuggestion) {
            return false
        }
        if (sender.isOp) {
            return true
        } else if (this.isOp) {
            return false
        }
        if (this.permission != "" && !sender.hasPermission(this.permission)) {
            return false
        }
        return true
    }
}