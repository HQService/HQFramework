package kr.hqservice.framework.command

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

open class RegisteredCommandTree(
    val declaredAt: KClass<*>,
    override val label: String,
    override val priority: Int,
    val permission: String,
    val isOp: Boolean,
    val hideSuggestion: Boolean
) : HQCommand, CommandSuggestible {
    private val commandExecutors: MutableMap<String, RegisteredCommandExecutor> = mutableMapOf()
    private val commandTrees: MutableMap<String, RegisteredCommandTree> = mutableMapOf()

    fun registerExecutor(executor: RegisteredCommandExecutor) {
        commandExecutors[executor.label] = executor
    }

    fun registerTree(tree: RegisteredCommandTree) {
        commandTrees[tree.label] = tree
    }

    internal fun findExecutor(key: String): RegisteredCommandExecutor? {
        return commandExecutors[key]
    }

    internal fun getSuggestions(sender: CommandSender): List<String> {
        return mutableListOf<CommandSuggestible>().apply {
            addAll(commandTrees.values.filter {
                !it.hideSuggestion && (sender.isOp || (!it.isOp && (it.permission.isEmpty() || sender.hasPermission(it.permission))))
            })
            addAll(commandExecutors.values.filter {
                !it.hideSuggestion && (sender.isOp || (!it.isOp && (it.permission.isEmpty() || sender.hasPermission(it.permission))))
            })
        }.sortedBy {
            it.priority
        }.map {
            it.label
        }
    }

    fun sendUsageMessages(target: CommandSender, where: Array<String>, pluginName: String) {
        val label = where.joinToString(" ")
        val components = getTextComponents(target, "", "/$label ")
        if (components.isNotEmpty()) {
            val component = TextComponent("/$label")
            component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/$label ")
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("클릭 시, 명령어를 입력합니다."))
            target.sendColorizedMessage("&f[<g:f58027>$pluginName</g:e8eb42>&f] <s:eded8e>Command Help Line")
            target.sendColorizedMessage("<s:9c9c83>&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            target.sendColorizedMessage("  &fnonnull parameter - <>")
            target.sendColorizedMessage("  &fnullable parameter - []")
            target.sendColorizedMessage("<s:9c9c83>&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            target.spigot().sendMessage(component)
            components.forEach {
                target.spigot().sendMessage(it)
            }
        } else target.sendMessage("§fUnknown command. Type \"/help\" for help.")
    }

    private fun getTextComponents(
        sender: CommandSender,
        padding: String = "",
        pointer: String = ""
    ): List<TextComponent> {
        val result = mutableListOf<TextComponent>()
        val filteredExecutors = commandExecutors.values.filter {
            sender.isOp || (!it.isOp && (it.permission.isEmpty() || sender.hasPermission(it.permission)))
        }
        for ((i, executor) in filteredExecutors.sortedBy { it.priority }.withIndex()) {
            val lastNode = (i + 1 == filteredExecutors.size) && commandTrees.isEmpty()
            val prefix = if (lastNode) " §7┗━§f" else " §7┣━§f"
            val parameters =
                executor.function.valueParameters.toMutableList().apply { removeFirst() }.joinToString("") {
                    val argumentLabel = it.findAnnotation<ArgumentLabel>()?.label ?: it.name!!
                    if (it.type.isMarkedNullable || it.isOptional) {
                        "[${argumentLabel}] "
                    } else {
                        "<${argumentLabel}> "
                    }
                }
            val component =
                TextComponent((padding + prefix + executor.label + " " + parameters + "&7" + executor.description).colorize())
            component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "$pointer${executor.label} ")
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("클릭 시, 명령어를 입력합니다."))

            result.add(component)
        }
        for ((i, child) in commandTrees.values.sortedBy { it.priority }.withIndex()) {
            val lastTree = i + 1 == commandTrees.size
            val component = TextComponent(padding + (if (lastTree) " §7┗━§f" else " §7┣━§f") + child.label)
            component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "$pointer${child.label} ")
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("클릭 시, 명령어를 입력합니다."))
            val childComponents = child.getTextComponents(
                sender,
                "$padding${if (lastTree) " " else "§7 ┃§f"}   ",
                pointer + child.label + " "
            )
            if (childComponents.isNotEmpty()) {
                result.add(component)
                result.addAll(childComponents)
            }
        }
        return result
    }

    protected fun findTreeAll(): MutableSet<RegisteredCommandTree> {
        val result = mutableSetOf<RegisteredCommandTree>()
        findTreeAll(this@RegisteredCommandTree, result)
        return result
    }

    protected fun findTreeAll(tree: RegisteredCommandTree, result: MutableSet<RegisteredCommandTree>) {
        result.add(tree)
        for (child in tree.commandTrees.values) {
            findTreeAll(child, result)
        }
    }

    fun findTreeExact(arguments: Array<String>): RegisteredCommandTree? {
        var tree: RegisteredCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument] ?: return@findTreeExact null
        }
        return tree
    }

    fun findTreeApproximate(arguments: Array<String>): RegisteredCommandTree {
        var tree: RegisteredCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument] ?: return tree
        }
        return tree
    }

    fun findTreeApproximateIndexed(arguments: Array<String>): Pair<Int, RegisteredCommandTree> {
        var tree: RegisteredCommandTree = this
        var index = 0
        arguments.forEach { argument ->
            index++
            tree = tree.commandTrees[argument] ?: return index to tree
        }
        return index to tree
    }
}