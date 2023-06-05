package kr.hqservice.framework.command.component

import kotlin.reflect.KFunction

class HQCommandExecutor(
    override val label: String,
    val description: String = "",
    val permission: String = "",
    val isOp: Boolean = false,
    val hideSuggestion: Boolean = false,
    override val priority: Int = 0,
    val function: KFunction<Unit>
) : CommandSuggestible {
    constructor(annotation: CommandExecutor, function: KFunction<Unit>) : this(
        annotation.label,
        annotation.description,
        annotation.permission,
        annotation.isOp,
        annotation.hideSuggestion,
        annotation.priority,
        function
    )
}