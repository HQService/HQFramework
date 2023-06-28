package kr.hqservice.framework.bukkit.core.extension

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private var pattern = "yyyy년 MM월 dd일 HH시 mm분 ss초"

private var formatter = DateTimeFormatter.ofPattern(pattern)

fun setPattern(value: String) {
    pattern = value
    formatter = DateTimeFormatter.ofPattern(value)
}

fun String.toLocalDateTime() = LocalDateTime.parse(this, formatter)

fun LocalDateTime.toFormattedTime() = format(formatter)

fun ZonedDateTime.getBetweenSeconds() = Duration.between(LocalDateTime.now(), this).seconds

fun ZonedDateTime.isTimeAfter() = ZonedDateTime.now().isAfter(this)