package kr.hqservice.framework.bukkit.core.extension

import java.lang.IllegalArgumentException
import kotlin.math.ln
import kotlin.random.Random

private val random by lazy { Random }

fun Int.percent(): Boolean = toDouble().percent()

fun Double.percent(): Boolean {
    return if (this >= 100.0) true
    else if (this <= 0.0) false
    else {
        val successful = this
        val fail = 100.0 - successful
        mapOf(true to successful, false to fail).random()
    }
}

fun <T> Map<T, Double>.random(): T {
    val entry = entries.minByOrNull { -ln(random.nextDouble()) / it.value }
    return entry?.key ?: throw IllegalArgumentException()
}