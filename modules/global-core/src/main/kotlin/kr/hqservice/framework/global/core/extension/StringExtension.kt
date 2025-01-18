package kr.hqservice.framework.global.core.extension

fun String.replaces(vararg replaces: Pair<String, Any>): String {
    var text = this
    replaces.forEach {
        text = text.replace(it.first, it.second.toString())
    }
    return text
}

fun String.removeSpace(): String {
    return replace(" ", "")
}