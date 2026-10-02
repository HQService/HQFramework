package kr.hqservice.framework.command.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.command.*
import kr.hqservice.framework.command.registry.CommandArgumentExceptionHandlerRegistry
import kr.hqservice.framework.command.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.command.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.SimplePluginManager
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@AnnotationHandler
class CommandAnnotationHandler(
    private val pluginManager: PluginManager,
    private val commandRegistry: CommandRegistry,
    private val tabCompletionHandler: CommandTabCompletionHandler,
    private val argumentProviderRegistry: CommandArgumentProviderRegistry,
    private val commandArgumentExceptionHandlerRegistry: CommandArgumentExceptionHandlerRegistry
) : HQAnnotationHandler<Command> {
    private val paperUse = try {
        Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent")
        true
    } catch (_: ClassNotFoundException) { false }

    private val registeredCommands = ConcurrentHashMap<KClass<*>, HQBukkitCommand>()

    override fun setup(instance: Any, annotation: Command) {
        val plugin = PluginScopeFinder.get(instance::class)
        if (!hasParent(annotation)) {
            registerRoot(annotation.label, annotation.aliases.toList(), instance::class, plugin)
        }

        if (hasLabel(annotation)) {
            registerTree(annotation.parent, instance)
            registerExecutors(instance::class, instance)
        } else {
            registerExecutors(annotation.parent, instance)
        }

        if (paperUse) {
            tabCompletionHandler.initialize()
        }
    }

    override fun teardown(instance: Any, annotation: Command) {
        if (hasParent(annotation)) return
        val hqCommand = registeredCommands.remove(instance::class) ?: return
        tabCompletionHandler.unregisterTabCompletion(hqCommand)
        val commandMap = CommandMapAccess.commandMap(hqCommand.plugin.server) ?: return
        hqCommand.unregister(commandMap)
        CommandMapAccess.knownCommands(commandMap)?.values?.removeIf { it === hqCommand }
    }

    private fun hasParent(annotation: Command): Boolean {
        return annotation.parent != Any::class
    }

    private fun hasLabel(annotation: Command): Boolean {
        return annotation.label != ""
    }

    private fun registerTree(parent: KClass<*>, commandInstance: Any) {
        val tree = commandRegistry.registerTree(parent, commandInstance::class)
        commandRegistry.findTree(parent)?.registerTree(tree)
    }

    private fun registerExecutors(parent: KClass<*>, commandInstance: Any) {
        commandInstance::class.memberFunctions.filterIsInstance<KFunction<Unit>>().forEach { function ->
            function.annotations.filterIsInstance<CommandExecutor>().forEach { commandExecutor ->
                if (validateCommandExecutor(commandInstance::class, function)) {
                    val executor = commandRegistry.registerExecutor(parent, commandExecutor, commandInstance, function)
                    commandRegistry.findTree(parent)?.registerExecutor(executor)
                }
            }
        }
    }

    private fun validateCommandExecutor(commandClass: KClass<*>, function: KFunction<Unit>): Boolean {
        val where = "(where: ${commandClass.simpleName}#${function.name}"
        if (!function.valueParameters.first().type.jvmErasure.isSubclassOf(CommandSender::class)) {
            Logger.getAnonymousLogger().severe("CommandExecutor 의 첫번째 인자에는 CommandSender 를 상속받는 클래스가 와야합니다. $where")
            return false
        }
        var allowParameterNullable = true
        function.valueParameters.reversed().forEach { parameter ->
            if (parameter.type.isMarkedNullable && !allowParameterNullable) {
                Logger.getAnonymousLogger().severe("CommandExecutor 의 Nullable 타입은 Nonnull 타입 앞에 올 수 없습니다. $where")
                return false
            }
            allowParameterNullable = parameter.type.isMarkedNullable
        }

        return true
    }

    private fun registerRoot(label: String, aliases: List<String>, rootClass: KClass<*>, plugin: HQBukkitPlugin) {
        if (pluginManager !is SimplePluginManager) {
            plugin.logger.info("skipping registration while mocking")
            return
        }
        val commandMap = CommandMapAccess.commandMap(plugin.server) ?: return
        val root = commandRegistry.registerRoot(rootClass)

        val hqCommand = HQBukkitCommand(
            label,
            aliases,
            paperUse,
            plugin,
            root,
            argumentProviderRegistry,
            commandArgumentExceptionHandlerRegistry
        )
        hqCommand.permission = if (root.isOp) "op" else root.permission

        commandMap.register("hq", hqCommand)
        registeredCommands[rootClass] = hqCommand

        plugin.launch {
            bukkitDelay(1)
            setupTree(root)
            if (paperUse) {
                tabCompletionHandler.registerTabCompletion(label, hqCommand)
            }
        }
    }

    private fun setupTree(registeredCommandTree: RegisteredCommandTree) {
        commandRegistry.getExecutors(registeredCommandTree.declaredAt).forEach { executor ->
            registeredCommandTree.registerExecutor(executor)
        }

        commandRegistry.getTrees(registeredCommandTree.declaredAt).forEach { tree ->
            registeredCommandTree.registerTree(tree)
            setupTree(tree)
        }
    }

    internal class HQBukkitCommand(
        label: String,
        alias: List<String>,
        private val paperUse: Boolean,
        internal val plugin: HQBukkitPlugin,
        private val hqCommandRoot: RegisteredCommandRoot,
        private val registry: CommandArgumentProviderRegistry,
        private val exceptionHandlerRegistry: CommandArgumentExceptionHandlerRegistry
    ) : BukkitCommand(label, "", "", alias) {
        private val providerTabCompleteCache = ConcurrentHashMap<String, Pair<String, List<String>>>()

        @Suppress("DuplicatedCode") // 실제로 겹친 두 코드의 한쪽은 suspend fun 이기 때문에 다른 코드이다.
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            if (!hqCommandRoot.canUse(sender)) {
                sendPermissionDeclinedMessage(sender)
                return true
            }
            if (args.isEmpty()) {
                hqCommandRoot.sendUsageMessages(sender, arrayOf(commandLabel), plugin.name)
                return true
            }
            val resolution = when (val resolved = hqCommandRoot.resolveExecutor(sender, args)) {
                CommandResolution.Denied -> {
                    sendPermissionDeclinedMessage(sender)
                    return true
                }
                is CommandResolution.Usage -> {
                    resolved.tree.sendUsageMessages(sender, arrayOf(commandLabel, *resolved.treeKey), plugin.name)
                    return true
                }
                is CommandResolution.Found -> resolved
            }
            val treeKey = resolution.treeKey
            val executor = resolution.executor

            val senderInstance = when (executor.getCommandSenderType().jvmErasure) {
                CommandSender::class -> sender
                Player::class -> if (sender is Player) {
                    sender
                } else {
                    sender.sendColorizedMessage("&c플레이어만 사용할 수 있는 명령어입니다.")
                    return true
                }

                ConsoleCommandSender::class -> if (sender is ConsoleCommandSender) {
                    sender
                } else {
                    sender.sendColorizedMessage("&c콘솔에서만 사용할 수 있는 명령어입니다.")
                    return true
                }

                else -> throw IllegalArgumentException("not command sender")
            }

            val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
            plugin.launch(Dispatchers.Default) commandLaunch@{
                executor.function.parameters.forEach forEach@ { kParameter ->
                    val index = kParameter.index
                    val argumentLabel = findArgumentLabel(kParameter)
                    val argument: String? = args.getOrNull(index - 2 + (treeKey.size + 1))
                    if (index in 0..1) {
                        return@forEach
                    }
                    if (argument == null && kParameter.isOptional) {
                        return@forEach
                    }
                    // 함수 인자가 nullable 이면 생략한다.
                    if (kParameter.type.isMarkedNullable && argument == null) {
                        arguments[kParameter] = null
                        return@forEach
                    }
                    val parameterMap = executor.function.valueParameters
                        .toMutableList()
                        .apply { removeFirst() }
                        .associateBy { kParameter2 ->
                            args.getOrNull(kParameter2.index - 1 + treeKey.size)?.run {
                                this to kParameter2.index - 1 + treeKey.size
                            } ?: ("" to kParameter2.index - 1 + treeKey.size)
                        }
                    val commandContext = CommandContextImpl(senderInstance, argumentLabel ?: kParameter.name!!, parameterMap)
                    var isFailed = false
                    withContext(Dispatchers.IO) withContext@{
                        val argumentProvider = getArgumentProvider(kParameter)
                        val casted = try {
                            argumentProvider.cast(commandContext, argument)
                        } catch (throwable: Throwable) {
                            val handler = exceptionHandlerRegistry.find(throwable::class, sender::class)
                            if (handler != null) {
                                handler.handle(throwable, sender, commandContext, argument)
                                isFailed = true
                            } else {
                                throw throwable
                            }
                        }
                        if (casted == null) {
                            isFailed = true
                            return@withContext
                        }
                        arguments[kParameter] = casted
                    }
                    if (isFailed) {
                        return@commandLaunch
                    }
                }

                val function = executor.function
                val callArguments = arguments + mapOf(
                    function.instanceParameter!! to executor.executorInstance,
                    function.valueParameters.first() to senderInstance
                )
                if (function.isSuspend) {
                    withContext(Dispatchers.BukkitAsync) {
                        function.callSuspendBy(callArguments)
                    }
                } else {
                    withContext(Dispatchers.BukkitMain) {
                        function.callBy(callArguments)
                    }
                }
            }
            return true
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
            return this.tabComplete(sender, alias, args, null)
        }

        override fun tabComplete(
            sender: CommandSender,
            alias: String,
            args: Array<String>,
            location: Location?
        ): List<String> {
            if (paperUse) return emptyList()
            return completeTabs(sender, args) { kParameter, context ->
                val senderKey = (sender as? Entity)?.uniqueId?.toString() ?: sender.name
                val argumentsKey = args.dropLast(1).joinToString(" ")
                plugin.launch(Dispatchers.IO) {
                    providerTabCompleteCache[senderKey] =
                        argumentsKey to getArgumentProvider(kParameter).getTabComplete(context, location)
                }
                providerTabCompleteCache[senderKey]
                    ?.takeIf { it.first == argumentsKey }
                    ?.second
                    ?.filter { it.startsWith(args.last()) }
                    ?: emptyList()
            }
        }

        fun hqTabComplete(
            sender: CommandSender,
            alias: String,
            args: Array<String>,
            location: Location?
        ): List<String> {
            return completeTabs(sender, args) { kParameter, context ->
                runBlocking {
                    plugin.async(Dispatchers.IO) {
                        getArgumentProvider(kParameter)
                            .getTabComplete(context, location)
                            .filter { it.startsWith(args.last()) }
                    }.await()
                }
            }
        }

        @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
        private fun completeTabs(
            sender: CommandSender,
            args: Array<String>,
            completeArgument: (KParameter, CommandContext) -> List<String>
        ): List<String> {
            if (!hqCommandRoot.validateSuggestion(sender, true)) {
                return emptyList()
            }
            if (args.first().length == 0) {
                return hqCommandRoot.getSuggestions(sender).filter { it.startsWith(args.last()) }
            }
            val treeKey = hqCommandRoot.findTreeKeyApproximate(args)
            if (!hqCommandRoot.canUsePath(sender, treeKey)) {
                return emptyList()
            }
            val tree = hqCommandRoot.findTreeExact(treeKey)
            val treeKeyAfter = if (args.size != treeKey.size) {
                args[treeKey.size]
            } else {
                args[treeKey.size - 1]
            }
            val executor = tree?.findExecutor(treeKeyAfter)
            if (tree != null) {
                if (executor != null && treeKey.size + 1 != args.size) {
                    if (!executor.validateSuggestion(sender, true)) {
                        return emptyList()
                    }
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
                            (argument to index) to executor.function.valueParameters[index + 1]
                        }.toMap()
                    val context = CommandContextImpl(sender, findArgumentLabel(kParameter) ?: kParameter.name!!, parameterMap)
                    return completeArgument(kParameter, context)
                } else {
                    return tree.getSuggestions(sender).filter { it.startsWith(args.last()) }
                }
            }
            return emptyList()
        }

        private fun getArgumentProvider(parameter: KParameter): CommandArgumentProvider<*> {
            val classifier = parameter.type.jvmErasure
            val qualifier = parameter.findAnnotation<Qualifier>()?.value
            return registry.getProvider(classifier, qualifier)
        }

        private fun findArgumentLabel(kAnnotatedElement: KAnnotatedElement): String? {
            return kAnnotatedElement.findAnnotation<ArgumentLabel>()?.label
        }

        private fun sendPermissionDeclinedMessage(sender: CommandSender) {
            sender.sendMessage("§fUnknown command. Type \"/help\" for help.")
        }
    }
}