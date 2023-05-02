package kr.hqservice.framework.core.extensions

fun <T> T.print(prefix: String = "", suffix: String = ""): T = also { println(prefix + it + suffix) }