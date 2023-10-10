package kr.hqservice.framework.bungee.core.color.pattern

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.regex.Pattern

internal object Solid : TextColorPattern {
    private val pattern = Pattern.compile("<s:([0-9A-Fa-f]{6})>")

    override fun colorize(text: String): String {
        var result: String = text
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val hexColor = matcher.group(1)
            val color = ChatColor.of(Color(hexColor.toInt(16)))
            result = result.replace(matcher.group(), color.toString())
        }
        return result
    }
}