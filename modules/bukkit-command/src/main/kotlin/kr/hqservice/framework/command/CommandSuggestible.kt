package kr.hqservice.framework.command

interface CommandSuggestible {
    val priority: Int

    val label: String
}