package kr.hqservice.framework.command.component

import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.valueParameters

class HQCommandExecutor(
    override val label: String,
    val description: String? = null,
    val permission: String = "",
    val isOp: Boolean = false,
    val hideSuggestion: Boolean = false,
    override val priority: Int = 0,
    val nodeInstance: HQCommandNode,
    val function: KFunction<Unit>
) : CommandSuggestible {
    constructor(annotation: CommandExecutor, nodeInstance: HQCommandNode, function: KFunction<Unit>) : this(
        annotation.label,
        annotation.description,
        annotation.permission,
        annotation.isOp,
        annotation.hideSuggestion,
        annotation.priority,
        nodeInstance,
        function
    )

    fun getCommandSenderType(): KType {
        return function.valueParameters.first().type
    }
}