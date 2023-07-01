package kr.hqservice.framework.command.component

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.command.component.registry.CommandRegistry
import org.bukkit.command.CommandSender

abstract class HQCommandTree(
    override val label: String,
    override val priority: Int = 0
) : HQCommand, CommandSuggestible {
    private val commandExecutors: MutableMap<String, HQCommandExecutor> = mutableMapOf()
    private val commandTrees: MutableMap<String, HQCommandTree> = mutableMapOf()

    internal fun getExecutors(): Map<String, HQCommandExecutor> {
        return commandExecutors
    }

    internal fun findExecutor(key: String): HQCommandExecutor? {
        return commandExecutors[key]
    }

    fun getTrees(): Map<String, HQCommandTree> {
        return commandTrees
    }

    internal fun getSuggestions(): List<String> {
        return mutableListOf<CommandSuggestible>().apply {
            addAll(commandTrees.values)
            addAll(commandExecutors.values.filter { !it.hideSuggestion })
        }.sortedBy {
            it.priority
        }.map {
            it.label
        }
    }

    fun sendUsageMessages(target: CommandSender) {
        target.sendMessage(label)
        getDescriptions().forEach {
            target.sendMessage(it)
        }
        /*getUsageMessages().forEach {
            target.sendMessage(it)
        }*/
    }

    private fun getDescriptions(padding: String = ""): List<String> {
        val result = mutableListOf<String>()
        for((i, executor) in commandExecutors.values.sortedBy { it.priority }.withIndex()) {
            val lastNode = (i + 1 == commandExecutors.size) && commandTrees.isEmpty()
            val prefix = if(lastNode) "└ " else "├ "
            result.add((padding + prefix + executor.label + " " + executor.description).colorize())
        }
        for((i, child) in commandTrees.values.sortedBy { it.priority }.withIndex()) {
            val lastTree = i + 1 == commandTrees.size
            result.add(padding + (if(lastTree) "└ " else "├ ") + child.label)
            result.addAll(child.getDescriptions("$padding${if(lastTree) "" else "│"}   "))
        }
        return result
    }

    private fun getUsageMessages(): List<String> {
        val trees = mutableSetOf<HQCommandTree>()
        findTreeAll(this@HQCommandTree, trees)
        return trees.flatMap {
            it.getExecutors().values
        }.sortedBy {
            it.priority
        }.mapNotNull {
            it.description?.colorize()
        }
    }

    protected fun findTreeAll(): MutableSet<HQCommandTree> {
        val result = mutableSetOf<HQCommandTree>()
        findTreeAll(this@HQCommandTree, result)
        return result
    }

    protected fun findTreeAll(tree: HQCommandTree, result: MutableSet<HQCommandTree>) {
        result.add(tree)
        for (child in tree.commandTrees.values) {
            findTreeAll(child, result)
        }
    }

    fun findTreeExact(arguments: Array<String>): HQCommandTree? {
        var tree: HQCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument] ?: return@findTreeExact null
        }
        return tree
    }

    fun findTreeApproximate(arguments: Array<String>): HQCommandTree {
        var tree: HQCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument] ?: return tree
        }
        return tree
    }

    fun findTreeApproximateIndexed(arguments: Array<String>): Pair<Int, HQCommandTree> {
        var tree: HQCommandTree = this
        var index = 0
        arguments.forEach { argument ->
            index++
            tree = tree.commandTrees[argument] ?: return index to tree
        }
        return index to tree
    }

    internal fun setup(registry: CommandRegistry) {
        registry.getExecutors(this::class).forEach { (executorKey, executor) ->
            commandExecutors[executorKey] = executor
        }


        registry.getTrees(this::class).forEach { tree ->
            commandTrees[tree.label] = tree
            tree.setup(registry)
        }
    }
}