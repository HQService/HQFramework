package kr.hqservice.framework.global.core.extension

fun <T : Collection<String>> T.toHumanReadable(): String {
    var strBuilder: String? = null
    this.forEach forEach@{ string ->
        if (strBuilder == null) {
            strBuilder = string
            return@forEach
        }
        strBuilder = "$strBuilder, $string"
    }
    return strBuilder ?: ""
}
