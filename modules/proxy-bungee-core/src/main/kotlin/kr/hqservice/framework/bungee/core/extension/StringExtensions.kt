package kr.hqservice.framework.bungee.core.extension

import kr.hqservice.framework.bungee.core.color.pattern.Gradient
import kr.hqservice.framework.bungee.core.color.pattern.Solid
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.awt.Color

private val specialColors = arrayOf("&l", "&n", "&m", "&o", "&k", "§l", "§n", "§m", "§o", "§k")
private val patterns = listOf(Gradient, Solid)

internal fun String.withoutColor(specialOnly: Boolean = false): String {
    return if (!specialOnly) ChatColor.stripColor(this)
    else {
        var result = this
        for (color in specialColors)
            if (result.contains(color)) result = result.replace(color, "")
        return result
    }
}

internal fun String.applyColors(colors: List<ChatColor>): String {
    val specialColors = StringBuilder()
    val stringBuilder = StringBuilder()
    val chars = split("")
    var outIndex = 0
    var index = 0
    while (index++ < length) {
        val char = chars[index]
        if (char != "&" && char != "§")
            stringBuilder.append(colors[outIndex++]).append(specialColors).append(char)
        else if (index + 1 < chars.size) {
            if (chars[index + 1] == "r") specialColors.setLength(0)
            else {
                specialColors.append(char)
                specialColors.append(chars[index + 1])
            }
            index++
        } else stringBuilder.append(colors[outIndex++]).append(specialColors).append(char)
    }
    return stringBuilder.toString()
}

fun String.colorize(): String {
    var result = this
    patterns.forEach { result = it.colorize(result) }
    return ChatColor.translateAlternateColorCodes('&', result)
}

fun String.colorizedTextComponent(): TextComponent {
    return TextComponent(this.colorize()).newTextComponent()
}

fun String.gradient(startHexCode: String, endHexCode: String): String {
    return Gradient.createGradientString(this, Color(startHexCode.toInt(16)), Color(endHexCode.toInt(16)))
}

fun String.translateAlternateColorCodes(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}