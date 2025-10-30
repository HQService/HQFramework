package kr.hqservice.framework.velocity.core.pattern

sealed interface TextColorPattern {
    fun colorize(text: String): String
}