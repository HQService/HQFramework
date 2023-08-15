package kr.hqservice.framework.bukkit.core.extension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

fun LocalDateTime.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return format(DateTimeFormatter.ofPattern(pattern))
}

fun LocalDateTime.format(pattern: String = "yyyy-MM-dd HH:mm:ss", locale: Locale): String {
    return format(DateTimeFormatter.ofPattern(pattern, locale))
}

fun String.parseLocalDateTimeOrNull(pattern: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime? {
    return try {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
    } catch (exception: DateTimeParseException) {
        return null
    }
}