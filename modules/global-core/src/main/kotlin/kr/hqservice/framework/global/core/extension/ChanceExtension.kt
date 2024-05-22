package kr.hqservice.framework.global.core.extension

import kotlin.math.ln
import kotlin.random.Random

private val random: Random by lazy { Random }

fun Double.percent(): Boolean {
    return if (this >= 100.0) true
    else if (this <= 0.0) false
    else {
        val successful = this
        val fail = 100.0 - successful
        mapOf(true to successful, false to fail).random()
    }
}

fun Int.percent(): Boolean = toDouble().percent()

fun <T> Map<T, Double>.random(): T {
    return randomOrNull() ?: throw IllegalArgumentException()
}

fun <T> Map<T, Double>.randomOrNull(): T? {
    return entries.minByOrNull {
        -ln(random.nextDouble()) / it.value
    }?.key
}