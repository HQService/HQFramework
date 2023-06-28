package kr.hqservice.framework.bukkit.core.extension

import java.text.DecimalFormat

private const val DEFAULT_FORMAT = "#,##0.###"
private val decimalFormat = DecimalFormat(DEFAULT_FORMAT)

fun Number.toDecimalFormat(): String = decimalFormat.format(this)