package kr.hqservice.framework.command.component.registry.impl

import kr.hqservice.framework.command.component.HQCommand
import kr.hqservice.framework.command.component.HQCommandNode
import kr.hqservice.framework.command.component.HQCommandTree
import kr.hqservice.framework.command.component.registry.CommandRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

@Single(binds = [CommandRegistry::class])
class CommandRegistryImpl : CommandRegistry {
    private val commandNodes: MutableMap<KClass<out HQCommand>, MutableList<HQCommandNode>> = mutableMapOf()
    private val commandTrees: MutableMap<KClass<out HQCommand>, MutableList<HQCommandTree>> = mutableMapOf()

    override fun <T : HQCommandTree> addNode(parent: KClass<T>, node: HQCommandNode) {
        val parentTree = commandNodes[parent]
        if (parentTree == null) {
            commandNodes[parent] = mutableListOf(node)
        } else {
            parentTree.add(node)
        }
    }

    override fun <T : HQCommandTree> addTree(parent: KClass<T>, tree: HQCommandTree) {
        val parentTree = commandTrees[parent]
        if (parentTree == null) {
            commandTrees[parent] = mutableListOf(tree)
        } else {
            parentTree.add(tree)
        }
    }

    override fun <T : HQCommand> getNodes(parent: KClass<T>): List<HQCommandNode> {
        return commandNodes[parent] ?: emptyList()
    }

    override fun <T : HQCommand> getTrees(parent: KClass<T>): List<HQCommandTree> {
        return commandTrees[parent] ?: emptyList()
    }
}