package kr.hqservice.framework.command.component

import kr.hqservice.framework.command.component.registry.CommandArgumentProviderRegistry
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

abstract class HQCommandRoot(private val name: String) : HQCommandTree(name) {
    private companion object {
        const val NULLABLE_BOX = "[%s]"
        const val NONNULL_BOX = "<%s>"
    }

    open fun getFallbackPrefix(): String = "hq"

    internal fun register(commandMap: CommandMap, registry: CommandArgumentProviderRegistry) {
        commandMap.register(getFallbackPrefix(), HQBukkitCommand(registry))
    }

    private inner class HQBukkitCommand(private val registry: CommandArgumentProviderRegistry): BukkitCommand(name) {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            val treeKey = args.toMutableList().apply { removeLast() }
            val executorKey = args.last()
            val executor = findTreeExact(treeKey)?.findExecutor(executorKey)
            if (executor == null) {
                val approximateTree = findTreeApproximate(treeKey)
                approximateTree.getUsageMessages().forEach { message ->
                    sender.sendMessage(message)
                }
                return true
            }

            val commandSenderType = executor.function.parameters.first().type.jvmErasure
            if (!commandSenderType.isSubclassOf(CommandSender::class)) {
                throw IllegalStateException("HQCommandExecutor 의 첫번째 인자에는 CommandSender 를 상속받는 클래스가 와야합니다.")
            }
            val senderInstance = when(commandSenderType) {
                Player::class -> if (sender is Player) sender else return true
                ConsoleCommandSender::class -> if (sender is ConsoleCommandSender) sender else return true
                else -> throw IllegalArgumentException("not command sender")
            }

            val arguments: List<Any?> = listOf()
            executor.function.parameters.forEach forEach@{ parameter ->
                val index = parameter.index
                val argumentLabel = parameter.findAnnotation<ArgumentLabel>()?.label
                val argument: String? = args.getOrNull(index)
                if (index == 0) {
                    return@forEach
                } else {
                    val parameterTypeClassifier = parameter.type.classifier ?: throw IllegalStateException("parameter type cannot be intersection type")
                    val argumentProvider = registry.getProvider(parameterTypeClassifier)
                    val result = argumentProvider.getResult(senderInstance, argument)
                    if (!result) {
                        val failureMessage = argumentProvider.getFailureMessage(sender, argument, argumentLabel)
                        senderInstance.sendMessage(failureMessage)
                        return true
                    }

                }
            }
            executor.function.call(senderInstance, TODO())

            return true
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
            return this.tabComplete(sender, alias, args, null)
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>, location: Location?): List<String> {
            val treeKey = args.toList()
            val tree = findTreeExact(treeKey) ?: return emptyList()
            return tree.getSuggestions()
        }
    }
}