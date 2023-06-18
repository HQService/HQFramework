package kr.hqservice.framework.command.component.registry

import kr.hqservice.framework.command.component.HQCommand
import kr.hqservice.framework.command.component.HQCommandExecutor
import kr.hqservice.framework.command.component.HQCommandNode
import kr.hqservice.framework.command.component.HQCommandTree
import kotlin.reflect.KClass

interface CommandRegistry {
    fun <T : HQCommandTree> addNode(parent: KClass<T>, node: HQCommandNode)

    fun <T : HQCommandTree> addTree(parent: KClass<T>, tree: HQCommandTree)

    fun <T : HQCommand> getNodes(parent: KClass<T>): List<HQCommandNode>

    fun <T : HQCommandTree> getExecutors(tree: KClass<T>): Map<String, HQCommandExecutor>

    fun <T : HQCommand> getTrees(parent: KClass<T>): List<HQCommandTree>
}