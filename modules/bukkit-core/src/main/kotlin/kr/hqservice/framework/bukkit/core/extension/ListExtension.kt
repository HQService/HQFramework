package kr.hqservice.framework.bukkit.core.extension

fun List<String>.replace(vararg pairs: Pair<String, String>): List<String> {
    return map { it.replace(*pairs) }
}

fun <T> List<T>.getCount(value: T) = count { it == value }