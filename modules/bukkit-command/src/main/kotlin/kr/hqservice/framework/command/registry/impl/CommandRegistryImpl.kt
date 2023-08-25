package kr.hqservice.framework.command.registry.impl

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.Multimap
import kr.hqservice.framework.command.*
import kr.hqservice.framework.command.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.Bean
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@Bean
class CommandRegistryImpl : CommandRegistry {
    private val commandRoots: MutableMap<String, RegisteredCommandRoot> = mutableMapOf()
    private val commandTrees: BiMap<KClass<*>, RegisteredCommandTree> = HashBiMap.create()
    private val commandTreesByParentKey: Multimap<KClass<*>, RegisteredCommandTree> = ArrayListMultimap.create()
    private val commandExecutorsByParentKey: Multimap<KClass<*>, RegisteredCommandExecutor> = ArrayListMultimap.create()

    override fun registerRoot(declaredAt: KClass<*>): RegisteredCommandRoot {
        val annotation = declaredAt.findAnnotation<Command>()!!
        return RegisteredCommandRoot(declaredAt, annotation.label, annotation.permission, annotation.isOp, annotation.hideSuggestion).also { registeredCommandRoot ->
            commandRoots[annotation.label] = registeredCommandRoot
        }
    }

    override fun registerTree(parent: KClass<*>, treeClass: KClass<*>): RegisteredCommandTree {
        val treeAnnotation = treeClass.findAnnotation<Command>()
            ?: throw IllegalArgumentException("CommandTree annotation of ${treeClass.simpleName} is null")
        return RegisteredCommandTree(
            treeClass,
            treeAnnotation.label,
            treeAnnotation.priority,
            treeAnnotation.permission,
            treeAnnotation.isOp,
            treeAnnotation.hideSuggestion
        ).also { registeredCommandTree ->
            commandTreesByParentKey.put(parent, registeredCommandTree)
            commandTrees[treeClass] = registeredCommandTree
        }
    }

    override fun registerExecutor(
        parent: KClass<*>,
        annotation: CommandExecutor,
        executorInstance: Any,
        function: KFunction<Unit>
    ): RegisteredCommandExecutor {
        return RegisteredCommandExecutor(annotation, executorInstance, function).also { registeredCommandExecutor ->
            commandExecutorsByParentKey.put(parent, registeredCommandExecutor)
        }
    }

    override fun findRoot(label: String): RegisteredCommandRoot? {
        return commandRoots[label]
    }

    override fun findTree(treeClass: KClass<*>): RegisteredCommandTree? {
        return commandTrees[treeClass]
    }

    override fun getTrees(parent: KClass<*>): List<RegisteredCommandTree> {
        return commandTreesByParentKey[parent].toList()
    }

    override fun getExecutors(parent: KClass<*>): List<RegisteredCommandExecutor> {
        return commandExecutorsByParentKey[parent].toList()
    }
}