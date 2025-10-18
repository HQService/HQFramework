package kr.hqservice.framework.velocity.core.extension

import kr.hqservice.framework.velocity.core.pattern.Gradient
import kr.hqservice.framework.velocity.core.pattern.Solid
import java.awt.Color

private val specialColors = arrayOf("&l", "&n", "&m", "&o", "&k", "§l", "§n", "§m", "§o", "§k")
private val patterns = listOf(Gradient, Solid)

internal fun Color.toLegacyString(): String {
    val hex = red.toString(16) + blue.toString(16) + green.toString(16)
    return "§x${hex.map { "§$it" }.joinToString("")}"
}

internal fun String.withoutColor(specialOnly: Boolean = false): String {
    return if (!specialOnly) this.replace(Regex("§[0-9a-zA-Z]"), "")
    else {
        var result = this
        for (color in specialColors)
            if (result.contains(color)) result = result.replace(color, "")
        return result
    }
}

internal fun String.applyColors(colors: List<Color>): String {
    val specialColors = StringBuilder()
    val stringBuilder = StringBuilder()
    val chars = split("")
    var outIndex = 0
    var index = 0
    while (index++ < length) {
        val char = chars[index]
        if (char != "&" && char != "§")
            stringBuilder.append(colors[outIndex++].toLegacyString()).append(specialColors).append(char)
        else if (index + 1 < chars.size) {
            if (chars[index + 1] == "r") specialColors.setLength(0)
            else {
                specialColors.append(char)
                specialColors.append(chars[index + 1])
            }
            index++
        } else stringBuilder.append(colors[outIndex++].toLegacyString()).append(specialColors).append(char)
    }
    return stringBuilder.toString()
}

fun String.colorize(): String {
    var result = this
    patterns.forEach { result = it.colorize(result) }
    return result.replace("&","§")
}

fun String.gradient(startHexCode: String, endHexCode: String): String {
    return Gradient.createGradientString(this, Color(startHexCode.toInt(16)), Color(endHexCode.toInt(16)))
}