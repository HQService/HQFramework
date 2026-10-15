package kr.hqservice.framework.bukkit.core.color.pattern

import kr.hqservice.framework.bukkit.core.extension.applyColors
import kr.hqservice.framework.bukkit.core.extension.withoutColor
import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.roundToInt

internal object Gradient : TextColorPattern {
    private val pattern = Pattern.compile("<g:([0-9A-Fa-f]{6})>(.*?)</g:([0-9A-Fa-f]{6})>")

    private fun interpolate(first: Int, last: Int, ratio: Float): Int {
        return (first + (last - first) * ratio).roundToInt()
    }

    private fun generateGradient(firstColor: Color, lastColor: Color, size: Int): List<ChatColor> {
        if (size <= 1) return List(size) { ChatColor.of(firstColor) }

        return List(size) {
            val ratio = it.toFloat() / (size - 1)
            ChatColor.of(
                Color(
                    interpolate(firstColor.red, lastColor.red, ratio),
                    interpolate(firstColor.green, lastColor.green, ratio),
                    interpolate(firstColor.blue, lastColor.blue, ratio)
                )
            )
        }
    }

    fun createGradientString(text: String, first: Color, last: Color): String {
        val colors = generateGradient(first, last, text.withoutColor(true).length)
        return text.applyColors(colors)
    }

    override fun colorize(text: String): String {
        var result = text
        val matcher = pattern.matcher(result)
        while (matcher.find()) {
            val first = matcher.group(1)
            val content = matcher.group(2)
            val last = matcher.group(3)
            result = result.replace(
                matcher.group(),
                createGradientString(content, Color(first.toInt(16)), Color(last.toInt(16)))
            )
        }
        return result
    }
}