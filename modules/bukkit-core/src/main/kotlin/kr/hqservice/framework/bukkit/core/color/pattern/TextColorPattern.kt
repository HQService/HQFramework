package kr.hqservice.framework.bukkit.core.color.pattern

sealed interface TextColorPattern {
    fun colorize(text: String): String
}