package kr.hqservice.framework.command.component.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.command.component.*
import kr.hqservice.framework.command.component.impl.CommandContextImpl
import kr.hqservice.framework.command.component.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.coroutine.component.handler.CoroutineScopeComponentHandler
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler
import kr.hqservice.framework.global.core.extension.print
import org.bukkit.Location
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.SimplePluginManager
import org.koin.core.annotation.Named
import java.util.logging.Logger
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

@ComponentHandler(
    depends = [
        KoinModuleComponentHandler::class,
        CommandNodeComponentHandler::class,
        CommandTreeComponentHandler::class,
        CommandArgumentProviderComponentHandler::class,
        CoroutineScopeComponentHandler::class
    ]
)
class CommandRootComponentHandler(
    private val pluginManager: PluginManager,
    private val logger: Logger,
    private val commandRegistry: CommandRegistry,
    private val argumentProviderRepository: CommandArgumentProviderRegistry,
    @Named("command") private val commandCoroutineScope: CoroutineScope,
    @Named("main") private val mainCoroutineScope: CoroutineScope
) : HQComponentHandler<HQCommandRoot> {
    override fun setup(element: HQCommandRoot) {
        element.setup(commandRegistry)
        if (pluginManager is SimplePluginManager) {
            register(element, pluginManager)
        } else {
            logger.info("skipping registration while mocking")
        }
    }

    private fun register(hqCommandRoot: HQCommandRoot, pluginManager: SimplePluginManager) {
        val commandMap = SimplePluginManager::class
            .declaredMemberProperties
            .first { it.name == "commandMap" }
            .apply { isAccessible = true }
            .get(pluginManager) as CommandMap
        commandMap.register(
            hqCommandRoot.getFallbackPrefix(),
            HQBukkitCommand(hqCommandRoot, commandCoroutineScope, mainCoroutineScope, argumentProviderRepository)
        )
    }

    private class HQBukkitCommand(
        private val hqCommandRoot: HQCommandRoot,
        private val commandCoroutineScope: CoroutineScope,
        private val mainCoroutineScope: CoroutineScope,
        private val registry: CommandArgumentProviderRegistry
    ) : BukkitCommand(hqCommandRoot.name) {
        @Suppress("DuplicatedCode") // 실제로 겹친 두 코드의 한쪽은 suspend fun 이기 때문에 다른 코드이다.
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            if (args.isEmpty()) {
                hqCommandRoot.sendUsageMessages(sender)
                return true
            }
            val treeKey = findTreeKeyApproximate(args)

            val executorKey = if (args.size != treeKey.size) {
                args[treeKey.size]
            } else {
                args[treeKey.size - 1]
            }
            val tree = hqCommandRoot.findTreeExact(treeKey)
            val executor = tree?.findExecutor(executorKey)
            if (executor == null) {
                val approximateTree = hqCommandRoot.findTreeApproximate(treeKey)
                approximateTree.sendUsageMessages(sender)
                return true
            }

            val senderInstance = when (executor.getCommandSenderType().jvmErasure) {
                CommandSender::class -> sender
                Player::class -> if (sender is Player) {
                    sender
                } else {
                    sender.sendMessage("&c플레이어만 사용할 수 있는 명령어입니다.".colorize())
                    return true
                }

                ConsoleCommandSender::class -> if (sender is ConsoleCommandSender) {
                    sender
                } else {
                    sender.sendMessage("&c콘솔에서만 사용할 수 있는 명령어입니다.".colorize())
                    return true
                }

                else -> throw IllegalArgumentException("not command sender")
            }

            val arguments: MutableList<Any?> = mutableListOf()
            commandCoroutineScope.launch commandLaunch@{
                executor.function.parameters.forEach forEach@{ parameter ->
                    val index = parameter.index
                    val argumentLabel = findArgumentLabel(parameter)
                    val argument: String? = args.getOrNull(index - 2 + (treeKey.size + 1))
                    if (index in 0..1) {
                        return@forEach
                    }
                    // 함수 인자가 nullable 이면 생략한다.
                    if (parameter.type.isMarkedNullable && argument == null) {
                        arguments.add(null)
                        return@forEach
                    }

                    val parameterMap = executor.function.valueParameters
                        .toMutableList()
                        .apply { removeFirst() }
                        .associateBy { kParameter ->
                            args.getOrNull(kParameter.index - 1 + treeKey.size) ?: ""
                        }
                    val commandContext = CommandContextImpl(senderInstance, parameterMap)
                    val argumentForResult = if (argument.print("argument: ") == argumentLabel.print("label: ")) null else argument
                    when (val argumentProvider = getArgumentProvider(parameter)) {
                        is HQSuspendCommandArgumentProvider -> {
                            var isFailed = false
                            val coroutineContext = if (argumentProvider is CoroutineScope) {
                                argumentProvider.coroutineContext
                            } else {
                                commandCoroutineScope.coroutineContext
                            }
                            withContext(coroutineContext) withContext@{


                                val result = argumentProvider.getResult(commandContext, argumentForResult)
                                if (!result || argumentForResult == null) {
                                    val failureMessage =
                                        argumentProvider.getFailureMessage(commandContext, argumentForResult, argumentLabel)
                                    if (failureMessage != null) {
                                        senderInstance.sendMessage(failureMessage)
                                    }
                                    isFailed = true
                                    return@withContext
                                }


                                val casted = argumentProvider.cast(commandContext, argumentForResult)
                                arguments.add(casted)
                            }
                            if (isFailed) {
                                return@commandLaunch
                            }
                        }

                        is HQCommandArgumentProvider -> {
                            var isFailed = false
                            mainCoroutineScope.launch mainLaunch@{
                                val result = argumentProvider.getResult(commandContext, argumentForResult)
                                if (!result || argumentForResult == null) {
                                    val failureMessage =
                                        argumentProvider.getFailureMessage(commandContext, argumentForResult, argumentLabel)
                                    if (failureMessage != null) {
                                        senderInstance.sendMessage("&c$failureMessage".colorize())
                                    }
                                    isFailed = true
                                    return@mainLaunch
                                }
                                val casted = argumentProvider.cast(commandContext, argumentForResult)
                                arguments.add(casted)
                            }.join()

                            if (isFailed) {
                                return@commandLaunch
                            }
                        }
                    }
                }

                if (executor.isOp && !sender.isOp && sender !is ConsoleCommandSender) {
                    hqCommandRoot.sendPermissionDeclinedMessage(sender, null)
                    return@commandLaunch
                }

                if (executor.permission != "" && sender.hasPermission(executor.permission) && sender !is ConsoleCommandSender && sender.isOp.not()) {
                    hqCommandRoot.sendPermissionDeclinedMessage(sender, executor.permission)
                    return@commandLaunch
                }

                val function = executor.function
                if (function.isSuspend) {
                    if (executor.nodeInstance is CoroutineScope) {
                        withContext(executor.nodeInstance.coroutineContext) {
                            executor.function.callSuspend(
                                executor.nodeInstance,
                                senderInstance,
                                *arguments.toTypedArray()
                            )
                        }
                    } else executor.function.callSuspend(
                        executor.nodeInstance,
                        senderInstance,
                        *arguments.toTypedArray()
                    )
                } else {
                    mainCoroutineScope.launch {
                        executor.function.call(executor.nodeInstance, senderInstance, *arguments.toTypedArray())
                    }
                }
            }
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
                return hqCommandRoot.getSuggestions()
            }
            val treeKey = findTreeKeyApproximate(args)
            val tree = hqCommandRoot.findTreeExact(treeKey)
            val treeKeyAfter = if (args.size != treeKey.size) {
                args[treeKey.size]
            } else {
                args[treeKey.size - 1]
            }
            val executor = tree?.findExecutor(treeKeyAfter)
            if (tree != null) {
                if (executor != null && treeKey.size + 1 != args.size) {
                    if (treeKey.size + (executor.function.valueParameters.size - 1) + 1 < args.size) {
                        return emptyList()
                    }
                    val kParameter = executor.function.valueParameters[args.size - treeKey.size - 1]
                    val parameterMap = args
                        .toMutableList()
                        .apply {
                            repeat(treeKey.size + 1) {
                                this.removeFirst()
                            }
                        }.mapIndexed { index, argument ->
                            argument to executor.function.valueParameters[index + 1]
                        }.toMap()
                    val context = CommandContextImpl(sender, parameterMap)
                    return when (val argumentProvider = getArgumentProvider(kParameter)) {
                        is HQSuspendCommandArgumentProvider<*> -> runBlocking {
                            argumentProvider.getTabComplete(context, location, findArgumentLabel(kParameter))
                        }

                        is HQCommandArgumentProvider<*> -> argumentProvider.getTabComplete(
                            context,
                            location,
                            findArgumentLabel(kParameter)
                        )
                    }
                } else {
                    return tree.getSuggestions()
                }
            }
            return emptyList()
        }

        private fun findTreeKeyApproximate(arguments: Array<String>): Array<String> {
            return if (hqCommandRoot.findTreeExact(arguments) != null) {
                arguments
            } else {
                // tree 가 아니네? 그러면 뒤에있는건 노드일까 트리일까? 노드거나 그 노드의 파라미터일 수 있어.
                // tree 가 나올때까지 소거하자.
                val mutableArguments = arguments.toMutableList()
                while (hqCommandRoot.findTreeExact(mutableArguments.toTypedArray()) == null) {
                    mutableArguments.removeLast()
                }
                mutableArguments.toTypedArray()
            }
        }

        private fun getArgumentProvider(parameter: KParameter): CommandArgumentProvider<*> {
            val classifier =
                parameter.type.classifier ?: throw IllegalStateException("parameter type cannot be intersection type")
            return registry.getProvider(classifier)
        }

        private fun findArgumentLabel(kAnnotatedElement: KAnnotatedElement): String? {
            return kAnnotatedElement.findAnnotation<ArgumentLabel>()?.label
        }
    }
}