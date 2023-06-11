package kr.hqservice.framework.global.core.extension

import java.lang.Exception

fun <T : Any> T.throwIf(message: String = "",predication: (T) -> Boolean): T {
    if (predication(this)) {
        throw Exception(message)
    }
    return this
}