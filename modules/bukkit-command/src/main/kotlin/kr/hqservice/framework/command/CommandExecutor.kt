package kr.hqservice.framework.command

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandExecutor(
    val label: String,
    val description: String = "",
    val permission: String = "",
    val isOp: Boolean = false,
    val hideSuggestion: Boolean = false,
    val priority: Int = 0,
)