package kr.hqservice.framework.global.core.extension

fun <T> T.print(prefix: String = "", suffix: String = ""): T = also { println(prefix + it + suffix) }

fun <T> T.print(prefix: String = "", transform: (T) -> String?): T = also {
    println(prefix + transform(it))
}

fun <T> T.print(prefix: String = "", suffix: String,  transform: (T) -> String?): T = also {
    println(prefix + transform(it) + suffix)
}
