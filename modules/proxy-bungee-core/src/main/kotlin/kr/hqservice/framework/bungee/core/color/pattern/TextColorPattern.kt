package kr.hqservice.framework.bungee.core.color.pattern

sealed interface TextColorPattern {
    fun colorize(text: String): String
}