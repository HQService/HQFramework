package kr.hqservice.framework.core.extension

fun <T> T.print(prefix: String = "", suffix: String = ""): T = also { println(prefix + it + suffix) }