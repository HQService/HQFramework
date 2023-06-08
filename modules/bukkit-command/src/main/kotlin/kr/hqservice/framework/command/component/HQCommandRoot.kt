package kr.hqservice.framework.command.component

import kr.hqservice.framework.command.component.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.global.core.extension.print
import org.bukkit.Location
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

abstract class HQCommandRoot(private val name: String) : HQCommandTree(name) {
    open fun getFallbackPrefix(): String = "hq"

    internal fun register(commandMap: CommandMap, registry: CommandArgumentProviderRegistry) {
        commandMap.register(getFallbackPrefix(), HQBukkitCommand(registry))
    }

    /**
     * 명령어를 실행하는데 필요한 권한이 부족한 경우 플레이어에게 권한이 부족하다고 알려주는 메시지를 보냅니다.
     *
     * @param sender 입력한 사람
     * @param requiredPermission 필요한 권한 노드, 필요한 권한이 op 권한일 경우 null
     */
    open fun sendPermissionDeclinedMessage(sender: CommandSender, requiredPermission: String? = null) {
        sender.sendMessage("§c권한이 부족합니다.")
    }

    private inner class HQBukkitCommand(private val registry: CommandArgumentProviderRegistry) : BukkitCommand(name) {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            if (args.isEmpty()) {
                sendUsageMessages(sender)
                return true
            }
            val treeKey = findTreeKeyApproximate(args)

            val executorKey = if (args.size != treeKey.size) { args[treeKey.size] } else { args[treeKey.size - 1] }
            val tree = findTreeExact(treeKey)
            val executor = tree?.findExecutor(executorKey)
            if (executor == null) {
                val approximateTree = findTreeApproximate(treeKey)
                approximateTree.sendUsageMessages(sender)
                return true
            }

            val senderInstance = when (executor.getCommandSenderType().jvmErasure) {
                CommandSender::class -> sender
                Player::class -> if (sender is Player) sender else return true
                ConsoleCommandSender::class -> if (sender is ConsoleCommandSender) sender else return true
                else -> throw IllegalArgumentException("not command sender")
            }

            val arguments: MutableList<Any?> = mutableListOf()
            executor.function.parameters.forEach forEach@{ parameter ->
                val index = parameter.index
                val argumentLabel = findArgumentLabel(parameter)
                val argument: String? = args.getOrNull(index - 2 + (treeKey.size + 1))
                if (index in 0..1) {
                    return@forEach
                }
                val argumentProvider = getArgumentProvider(parameter)
                // 함수 인자가 nullable 이면 생략한다.
                if (parameter.type.isMarkedNullable && argument == null) {
                    arguments.add(null)
                    return@forEach
                }
                val result = argumentProvider.getResult(senderInstance, argument)
                if (!result || argument == null) {
                    val failureMessage = argumentProvider.getFailureMessage(sender, argument, argumentLabel)
                    if (failureMessage != null) {
                        senderInstance.sendMessage(failureMessage)
                    }
                    return true
                }
                val casted = argumentProvider.cast(argument)
                arguments.add(casted)
            }

            if (executor.isOp && !sender.isOp && sender !is ConsoleCommandSender) {
                sendPermissionDeclinedMessage(sender, null)
                return true
            }

            if (executor.permission != "" && sender.hasPermission(executor.permission) && sender !is ConsoleCommandSender) {
                sendPermissionDeclinedMessage(sender, executor.permission)
                return true
            }

            executor.function.call(executor.nodeInstance, senderInstance, *arguments.toTypedArray())
            return true
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
            return this.tabComplete(sender, alias, args, null)
        }

        @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
        override fun tabComplete(
            sender: CommandSender,
            alias: String,
            args: Array<String>,
            location: Location?
        ): List<String> {
            if (args.first().length == 0) {
                return getSuggestions()
            }
            val treeKey = findTreeKeyApproximate(args)
            val tree = findTreeExact(treeKey)
            val treeKeyAfter = if (args.size != treeKey.size) { args[treeKey.size] } else { args[treeKey.size - 1] }
            val executor = tree?.findExecutor(treeKeyAfter)
            if (tree != null) {
                if (executor != null && treeKey.size + 1 != args.size) {
                    if (treeKey.size + (executor.function.valueParameters.size - 1) + 1 < args.size) {
                        return emptyList()
                    }
                    val kParameter = executor.function.valueParameters[args.size - treeKey.size - 1]
                    val argumentProvider = getArgumentProvider(kParameter)
                    return argumentProvider.getTabComplete(sender, location, findArgumentLabel(kParameter))
                } else {
                    return tree.getSuggestions()
                }
            }
            return emptyList()
        }

        private fun findTreeKeyApproximate(arguments: Array<String>): Array<String> {
            return if (findTreeExact(arguments) != null) {
                arguments
            } else {
                // tree 가 아니네? 그러면 뒤에있는건 노드일까 트리일까? 노드거나 그 노드의 파라미터일 수 있어.
                // tree 가 나올때까지 소거하자.
                val mutableArguments = arguments.toMutableList()
                while (findTreeExact(mutableArguments.toTypedArray()) == null) {
                    mutableArguments.removeLast()
                }
                mutableArguments.toTypedArray()
            }
        }
        private fun getArgumentProvider(parameter: KParameter): HQCommandArgumentProvider<*> {
            val classifier = parameter.type.classifier ?: throw IllegalStateException("parameter type cannot be intersection type")
            return registry.getProvider(classifier)
        }

        private fun findArgumentLabel(kAnnotatedElement: KAnnotatedElement): String? {
            return kAnnotatedElement.findAnnotation<ArgumentLabel>()?.label
        }
    }
}