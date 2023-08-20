package kr.hqservice.framework.command

import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.valueParameters

data class RegisteredCommandExecutor(
    override val label: String,
    val description: String? = null,
    val permission: String = "",
    val isOp: Boolean = false,
    val hideSuggestion: Boolean = false,
    override val priority: Int = 0,
    val executorInstance: Any,
    val function: KFunction<Unit>
) : CommandSuggestible {
    constructor(annotation: CommandExecutor, executorInstance: Any, function: KFunction<Unit>) : this(
        annotation.label,
        annotation.description,
        annotation.permission,
        annotation.isOp,
        annotation.hideSuggestion,
        annotation.priority,
        executorInstance,
        function
    )

    fun getCommandSenderType(): KType {
        return function.valueParameters.first().type
    }
}