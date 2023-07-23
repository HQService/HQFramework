package kr.hqservice.framework.command.component.registry.impl

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import kr.hqservice.framework.command.component.*
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.command.CommandSender
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@Bean
class CommandRegistryImpl : CommandRegistry {
    private val commandNodes: Multimap<KClass<out HQCommand>, HQCommandNode> = ArrayListMultimap.create()
    private val commandTrees: Multimap<KClass<out HQCommand>, HQCommandTree> = ArrayListMultimap.create()
    private val commandExecutors: Multimap<KClass<out HQCommandTree>, Pair<String, HQCommandExecutor>> =
        ArrayListMultimap.create()

    override fun <T : HQCommandTree> addNode(parent: KClass<T>, node: HQCommandNode) {
        commandNodes.put(parent, node)
        node::class.memberFunctions.filterIsInstance<KFunction<Unit>>().forEach { function ->
            function.annotations.filterIsInstance<CommandExecutor>().forEach { commandExecutor ->
                if (validateCommandExecutor(function, node)) {
                    val executor = HQCommandExecutor(commandExecutor, node, function)
                    commandExecutors.put(parent, Pair(commandExecutor.label, executor))
                }
            }
        }
    }

    override fun <T : HQCommandTree> addTree(parent: KClass<T>, tree: HQCommandTree) {
        commandTrees.put(parent, tree)
    }

    override fun <T : HQCommand> getNodes(parent: KClass<T>): List<HQCommandNode> {
        return commandNodes.get(parent).toList()
    }

    override fun <T : HQCommand> getTrees(parent: KClass<T>): List<HQCommandTree> {
        return commandTrees.get(parent).toList()
    }

    override fun <T : HQCommandTree> getExecutors(tree: KClass<T>): Map<String, HQCommandExecutor> {
        return commandExecutors.get(tree).toMap()
    }

    private fun validateCommandExecutor(function: KFunction<Unit>, node: HQCommandNode): Boolean {
        val where = "(where: ${node::class.simpleName}#${function.name}"
        if (!function.valueParameters.first().type.jvmErasure.isSubclassOf(CommandSender::class)) {
            Logger.getAnonymousLogger().severe("HQCommandExecutor 의 첫번째 인자에는 CommandSender 를 상속받는 클래스가 와야합니다. $where")
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
}