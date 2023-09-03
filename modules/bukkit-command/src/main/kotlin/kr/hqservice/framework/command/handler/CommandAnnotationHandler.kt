package kr.hqservice.framework.command.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
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
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.SimplePluginManager
import java.util.logging.Logger
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

@AnnotationHandler
class CommandAnnotationHandler(
    private val pluginManager: PluginManager,
    private val commandRegistry: CommandRegistry,
    private val argumentProviderRegistry: CommandArgumentProviderRegistry,
    private val commandArgumentExceptionHandlerRegistry: CommandArgumentExceptionHandlerRegistry
) : HQAnnotationHandler<Command> {
    override fun setup(instance: Any, annotation: Command) {
        val plugin = PluginScopeFinder.get(instance::class)
        if (!hasParent(annotation)) {
            registerRoot(annotation.label, instance::class, plugin)
        }
        if (hasLabel(annotation)) {
            registerTree(annotation.parent, instance)
            registerExecutors(instance::class, instance)
        } else {
            registerExecutors(annotation.parent, instance)
        }
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

    private fun registerRoot(label: String, rootClass: KClass<*>, plugin: HQBukkitPlugin) {
        if (pluginManager !is SimplePluginManager) {
            plugin.logger.info("skipping registration while mocking")
            return
        }
        val root = commandRegistry.registerRoot(rootClass)

        val commandMap = SimplePluginManager::class
            .declaredMemberProperties
            .first { it.name == "commandMap" }
            .apply { isAccessible = true }
            .get(pluginManager) as CommandMap
        commandMap.register(
            "hq",
            HQBukkitCommand(label, plugin, root, argumentProviderRegistry, commandArgumentExceptionHandlerRegistry)
        )

        plugin.launch {
            bukkitDelay(1)
            setupTree(root)
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

    private class HQBukkitCommand(
        label: String,
        private val plugin: HQBukkitPlugin,
        private val hqCommandRoot: RegisteredCommandRoot,
        private val registry: CommandArgumentProviderRegistry,
        private val exceptionHandlerRegistry: CommandArgumentExceptionHandlerRegistry
    ) : BukkitCommand(label) {

        override fun getPermission(): String {
            return if (hqCommandRoot.isOp) "op" else hqCommandRoot.permission
        }

        @Suppress("DuplicatedCode") // 실제로 겹친 두 코드의 한쪽은 suspend fun 이기 때문에 다른 코드이다.
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            if (!hqCommandRoot.validateSuggestion(sender)) {
                sendPermissionDeclinedMessage(sender)
                return true
            }
            if (args.isEmpty()) {
                hqCommandRoot.sendUsageMessages(sender, arrayOf(commandLabel), plugin.name)
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
                approximateTree.sendUsageMessages(sender, arrayOf(commandLabel, *treeKey), plugin.name)
                return true
            }
            if (!executor.validateSuggestion(sender)) {
                sendPermissionDeclinedMessage(sender)
                return true
            }

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

            val arguments: MutableList<Any?> = mutableListOf()
            plugin.launch(Dispatchers.Default) commandLaunch@{
                executor.function.parameters.forEach forEach@{ kParameter ->
                    val index = kParameter.index
                    val argumentLabel = findArgumentLabel(kParameter)
                    val argument: String? = args.getOrNull(index - 2 + (treeKey.size + 1))
                    if (index in 0..1) {
                        return@forEach
                    }
                    // 함수 인자가 nullable 이면 생략한다.
                    if (kParameter.type.isMarkedNullable && argument == null) {
                        arguments.add(null)
                        return@forEach
                    }
                    val parameterMap = executor.function.valueParameters
                        .toMutableList()
                        .apply { removeFirst() }
                        .associateBy { kParameter2 ->
                            args.getOrNull(kParameter2.index - 1 + treeKey.size) ?: ""
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
                        arguments.add(casted)
                    }
                    if (isFailed) {
                        return@commandLaunch
                    }
                }

                if (executor.isOp && !sender.isOp && sender !is ConsoleCommandSender) {
                    sendPermissionDeclinedMessage(sender)
                    return@commandLaunch
                }

                val function = executor.function
                if (function.isSuspend) {
                    executor.function.callSuspend(executor.executorInstance, senderInstance, *arguments.toTypedArray())
                } else {
                    executor.function.call(executor.executorInstance, senderInstance, *arguments.toTypedArray())
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
            if (!hqCommandRoot.validateSuggestion(sender, true)) {
                return emptyList()
            }
            if (args.first().length == 0) {
                return hqCommandRoot.getSuggestions(sender).filter { it.startsWith(args.last()) }
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
                            argument to executor.function.valueParameters[index + 1]
                        }.toMap()
                    val context = CommandContextImpl(sender, findArgumentLabel(kParameter) ?: kParameter.name!!, parameterMap)
                    return runBlocking {
                        plugin.async(Dispatchers.IO) {
                            getArgumentProvider(kParameter)
                                .getTabComplete(context, location)
                                .filter { it.startsWith(args.last()) }
                        }.await()
                    }
                } else {
                    return tree.getSuggestions(sender).filter { it.startsWith(args.last()) }
                }
            }
            return emptyList()
        }

        private fun findTreeKeyApproximate(arguments: Array<String>): Array<String> {
            return if (hqCommandRoot.findTreeExact(arguments) != null) {
                arguments
            } else {
                val mutableArguments = arguments.toMutableList()
                while (hqCommandRoot.findTreeExact(mutableArguments.toTypedArray()) == null) {
                    mutableArguments.removeLast()
                }
                mutableArguments.toTypedArray()
            }
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
            sender.sendMessage("§c권한이 부족합니다.")
        }
    }
}