package kr.hqservice.framework.bukkit.core.extension

import net.md_5.bungee.api.ChatColor

fun String.colorize(): String {
    return this.replace("&", "§")
}

fun String.translateAlternateColorCodes(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}