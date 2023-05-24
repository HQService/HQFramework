package kr.hqservice.framework.global.core.extension

fun <T> T.print(prefix: String = "", suffix: String = ""): T = also { println(prefix + it + suffix) }