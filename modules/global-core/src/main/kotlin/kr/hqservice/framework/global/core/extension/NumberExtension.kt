package kr.hqservice.framework.global.core.extension

import java.text.DecimalFormat

private const val DEFAULT_FORMAT = "#,###.##"
private val decimalFormat = DecimalFormat(DEFAULT_FORMAT)

fun Double.format(): String = decimalFormat.format(this)

fun Float.format(): String = decimalFormat.format(this)

fun Long.format(): String = decimalFormat.format(this)

fun Int.format(): String = decimalFormat.format(this)