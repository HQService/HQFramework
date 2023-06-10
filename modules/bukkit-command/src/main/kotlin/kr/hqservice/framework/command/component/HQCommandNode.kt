package kr.hqservice.framework.command.component

import kotlin.reflect.KFunction

abstract class HQCommandNode : HQCommand {
    private val executors: MutableMap<String, HQCommandExecutor> = mutableMapOf()

    internal fun addExecutor(annotation: CommandExecutor, function: KFunction<Unit>) {
        executors[annotation.label] = HQCommandExecutor(annotation, this, function)
    }

    internal fun findExecutor(key: String): HQCommandExecutor? {
        return executors[key]
    }

    internal fun getExecutors(): Map<String, HQCommandExecutor> {
        return executors
    }
}