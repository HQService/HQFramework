package kr.hqservice.framework.command.registry

import kr.hqservice.framework.command.CommandExecutor
import kr.hqservice.framework.command.RegisteredCommandExecutor
import kr.hqservice.framework.command.RegisteredCommandRoot
import kr.hqservice.framework.command.RegisteredCommandTree
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface CommandRegistry {
    fun registerRoot(declaredAt: KClass<*>): RegisteredCommandRoot

    fun registerTree(parent: KClass<*>, treeClass: KClass<*>): RegisteredCommandTree

    fun registerExecutor(
        parent: KClass<*>,
        annotation: CommandExecutor,
        executorInstance: Any,
        function: KFunction<Unit>
    ): RegisteredCommandExecutor

    fun findRoot(label: String): RegisteredCommandRoot?

    fun findTree(treeClass: KClass<*>): RegisteredCommandTree?

    fun getTrees(parent: KClass<*>): List<RegisteredCommandTree>

    fun getExecutors(parent: KClass<*>): List<RegisteredCommandExecutor>
}