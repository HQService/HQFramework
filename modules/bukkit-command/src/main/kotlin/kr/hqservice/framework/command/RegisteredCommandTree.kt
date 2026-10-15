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
    override val aliases: List<String>,
    override val priority: Int,
    override val permission: String,
    override val isOp: Boolean,
    override val hideSuggestion: Boolean
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
        return commandExecutors[key] ?: commandExecutors.values.firstOrNull { it.aliases.contains(key) }
    }

    internal fun getSuggestions(sender: CommandSender): List<String> {
        return mutableListOf<CommandSuggestible>().apply {
            addAll(commandTrees.values.filter {
                it.validateSuggestion(sender, true)
            })
            addAll(commandExecutors.values.filter {
                it.validateSuggestion(sender, true)
            })
        }.sortedBy {
            it.priority
        }.flatMap {
            it.aliases +
            listOf(it.label)
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
        } else {
            target.sendMessage("§fUnknown command. Type \"/help\" for help.")
        }
    }

    internal fun getTextComponents(
        sender: CommandSender,
        padding: String = "",
        pointer: String = ""
    ): List<TextComponent> {
        val result = mutableListOf<TextComponent>()
        val filteredExecutors = commandExecutors.values.filter {
            it.validateSuggestion(sender, true)
        }
        val filteredTrees = commandTrees.values.filter {
            it.validateSuggestion(sender, true)
        }
        for ((i, executor) in filteredExecutors.sortedBy { it.priority }.withIndex()) {
            val lastNode = (i + 1 == filteredExecutors.size) && filteredTrees.isEmpty()
            val prefix = if (lastNode) " §7┗━§f" else " §7┣━§f"
            val parameters = executor.function.valueParameters
                .toMutableList()
                .apply { removeFirst() }
                .joinToString("") {
                    val argumentLabel = it.findAnnotation<ArgumentLabel>()?.label ?: it.name!!
                    if (it.type.isMarkedNullable || it.isOptional) {
                        "[${argumentLabel}] "
                    } else {
                        "<${argumentLabel}> "
                    }
                }
            val baseLabel = executor.label
            val finalLabel = if (pointer.matches(koreanRegex)) {
                executor.aliases.find {
                    it.matches(koreanRegex)
                } ?: baseLabel
            } else {
                baseLabel
            }
            val component =
                TextComponent((padding + prefix + finalLabel + " " + parameters + "&7" + executor.description).colorize())
            component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "$pointer${executor.label} ")
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("클릭 시, 명령어를 입력합니다."))

            result.add(component)
        }
        for ((i, child) in filteredTrees.sortedBy { it.priority }.withIndex()) {
            val lastTree = i + 1 == filteredTrees.size
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

    internal fun findTreePath(arguments: Array<String>): List<RegisteredCommandTree>? {
        val path = mutableListOf(this)
        arguments.forEach { argument ->
            val tree = path.last()
            path += tree.commandTrees[argument]
                ?: tree.commandTrees.values.firstOrNull { it.aliases.contains(argument) } ?: return@findTreePath null
        }
        return path
    }

    fun findTreeExact(arguments: Array<String>): RegisteredCommandTree? {
        return findTreePath(arguments)?.last()
    }

    internal fun canUsePath(sender: CommandSender, treeKey: Array<String>): Boolean {
        return findTreePath(treeKey)?.all { it.canUse(sender) } ?: false
    }

    internal fun findTreeKeyApproximate(arguments: Array<String>): Array<String> {
        var treeKey = arguments.toList()
        while (findTreeExact(treeKey.toTypedArray()) == null) {
            treeKey = treeKey.dropLast(1)
        }
        return treeKey.toTypedArray()
    }

    internal fun resolveExecutor(sender: CommandSender, arguments: Array<String>): CommandResolution {
        val treeKey = findTreeKeyApproximate(arguments)
        if (!canUsePath(sender, treeKey)) {
            return CommandResolution.Denied
        }
        val tree = findTreeExact(treeKey)!!
        val executorKey = arguments.getOrNull(treeKey.size) ?: treeKey.last()
        val executor = tree.findExecutor(executorKey) ?: return CommandResolution.Usage(treeKey, tree)
        if (!executor.canUse(sender)) {
            return CommandResolution.Denied
        }
        return CommandResolution.Found(treeKey, executor)
    }

    fun findTreeApproximate(arguments: Array<String>): RegisteredCommandTree {
        var tree: RegisteredCommandTree = this
        arguments.forEach { argument ->
            tree = tree.commandTrees[argument]
                ?: tree.commandTrees.values.firstOrNull { it.aliases.contains(argument) } ?: return tree
        }
        return tree
    }

    private companion object {
        val koreanRegex = Regex(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")
    }
}

internal sealed interface CommandResolution {
    data object Denied : CommandResolution
    class Usage(val treeKey: Array<String>, val tree: RegisteredCommandTree) : CommandResolution
    class Found(val treeKey: Array<String>, val executor: RegisteredCommandExecutor) : CommandResolution
}
