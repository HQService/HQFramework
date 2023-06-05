package kr.hqservice.framework.command.component

interface CommandSuggestible {
    val priority: Int

    val label: String
}