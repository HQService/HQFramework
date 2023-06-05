package kr.hqservice.framework.command.component

import kr.hqservice.framework.command.component.registry.CommandRegistry

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

    internal fun getTrees(): Map<String, HQCommandTree> {
        return commandTrees
    }

    internal fun getSuggestions(): List<String> {
        return mutableListOf<CommandSuggestible>().apply {
            addAll(commandTrees.values)
            addAll(commandExecutors.values)
        }.sortedBy {
            it.priority
        }.map {
            it.label
        }
    }

    internal fun getUsageMessages(): List<String> {
        val trees = mutableSetOf<HQCommandTree>()
        findTreeAll(this@HQCommandTree, trees)
        return trees.flatMap {
            it.getExecutors().values
        }.sortedBy {
            it.priority
        }.map {
            it.description
        }
    }

    protected fun findTreeAll(tree: HQCommandTree, result: MutableSet<HQCommandTree>) {
        result.add(tree)
        for (child in tree.commandTrees.values) {
            findTreeAll(child, result)
        }
    }

    protected fun findTreeExact(arguments: List<String>): HQCommandTree? {
        var tree: HQCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument] ?: return@findTreeExact null
        }
        return tree
    }

    protected fun findTreeApproximate(arguments: List<String>): HQCommandTree {
        var tree: HQCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument] ?: return tree
        }
        return tree
    }

    internal fun setup(repository: CommandRegistry) {
        repository.getNodes(this::class).forEach { node ->
            node.getExecutors().forEach { (executorKey, executor) ->
                commandExecutors[executorKey] = executor
            }
        }

        repository.getTrees(this::class).forEach { tree ->
            commandTrees[tree.label] = tree
            tree.setup(repository)
        }
    }
}