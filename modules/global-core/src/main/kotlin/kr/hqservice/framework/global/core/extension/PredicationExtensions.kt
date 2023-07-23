package kr.hqservice.framework.global.core.extension

fun <T : Any> T.throwIf(message: String = "", predication: (T) -> Boolean): T {
    if (predication(this)) {
        throw Exception(message)
    }
    return this
}