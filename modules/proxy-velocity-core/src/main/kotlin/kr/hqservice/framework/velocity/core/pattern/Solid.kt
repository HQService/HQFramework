package kr.hqservice.framework.velocity.core.pattern

import java.awt.Color
import java.util.regex.Pattern

internal object Solid : TextColorPattern {
    private val pattern = Pattern.compile("<s:([0-9A-Fa-f]{6})>")

    override fun colorize(text: String): String {
        var result: String = text
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val hexColor = matcher.group(1)
            val new = hexColor.map { "§$it" }.joinToString("")
            val color = "§x$new"
            result = result.replace(matcher.group(), color)
        }
        return result
    }
}