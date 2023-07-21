package kr.hqservice.framework.bukkit.core.color.pattern

import kr.hqservice.framework.bukkit.core.extension.applyColors
import kr.hqservice.framework.bukkit.core.extension.withoutColor
import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.abs

internal object Gradient : TextColorPattern {
    private val pattern = Pattern.compile("<g:([0-9A-Fa-f]{6})>(.*?)</g:([0-9A-Fa-f]{6})>")

    private fun getDirection(first: Int, last: Int): Int {
        return if (first < last) 1 else -1
    }

    private fun generateGradient(firstColor: Color, lastColor: Color, size: Int): List<ChatColor> {
        val r = abs(firstColor.red - lastColor.red) / (size - 1)
        val g = abs(firstColor.green - lastColor.green) / (size - 1)
        val b = abs(firstColor.blue - lastColor.blue) / (size - 1)
        val redDirection = getDirection(firstColor.red, lastColor.red)
        val greenDirection = getDirection(firstColor.green, lastColor.green)
        val blueDirection = getDirection(firstColor.blue, lastColor.blue)

        return List(size) {
            ChatColor.of(
                Color(
                    firstColor.red + r * it * redDirection,
                    firstColor.green + g * it * greenDirection,
                    firstColor.blue + b * it * blueDirection
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