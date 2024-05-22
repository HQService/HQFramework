package kr.hqservice.framework.bukkit.core.extension

import org.bukkit.entity.Player

fun Player.executeCommand(command: String): Boolean {
    val op = isOp
    isOp = true
    try {
        performCommand(command)
    } finally {
        if (!op) isOp = false
    }
    return true
}