package kr.hqservice.framework.command.component

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.command.component.registry.CommandRegistry
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

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

    fun sendUsageMessages(target: CommandSender, where: Array<String>, pluginName: String) {
        val label = where.joinToString(" ")
        target.sendColorizedMessage("&f[<g:f58027>$pluginName</g:e8eb42>&f] <s:eded8e>Command Help Line")
        target.sendColorizedMessage("<s:9c9c83>&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        target.sendColorizedMessage("  &fnonnull parameter - <>")
        target.sendColorizedMessage("  &fnullable parameter - []")
        target.sendColorizedMessage("<s:9c9c83>&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        target.sendColorizedMessage("/$label")
        getTextComponents("", "/$label ").forEach {
            target.spigot().sendMessage(it)
        }
    }

    private fun getTextComponents(padding: String = "", pointer: String = ""): List<TextComponent> {
        val result = mutableListOf<TextComponent>()
        for((i, executor) in commandExecutors.values.sortedBy { it.priority }.withIndex()) {
            val lastNode = (i + 1 == commandExecutors.size) && commandTrees.isEmpty()
            val prefix = if(lastNode) " §7┗━§f" else " §7┣━§f"
            val parameters =
                executor.function.valueParameters.toMutableList().apply { removeFirst() }.joinToString("") {
                    val argumentLabel = it.findAnnotation<ArgumentLabel>()?.label ?: it.name!!
                    if (it.type.isMarkedNullable || it.isOptional) {
                        "[${argumentLabel}] "
                    } else {
                        "<${argumentLabel}> "
                    }
                }
            val component = TextComponent((padding + prefix + executor.label + " " + parameters + "&7" +  executor.description).colorize())
            component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "$pointer${executor.label} ")
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("클릭 시, 명령어를 입력합니다."))

            result.add(component)
        }
        for((i, child) in commandTrees.values.sortedBy { it.priority }.withIndex()) {
            val lastTree = i + 1 == commandTrees.size
            val component = TextComponent(padding + (if(lastTree) " §7┗━§f" else " §7┣━§f") + child.label)
            component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "$pointer${child.label} ")
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("클릭 시, 명령어를 입력합니다."))
            result.add(component)
            result.addAll(child.getTextComponents("$padding${if(lastTree) " " else " ┃"}   ", pointer + child.label + " "))
        }
        return result
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