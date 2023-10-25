package kr.hqservice.framework.command

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import org.bukkit.command.CommandSender
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

internal class CommandContextImpl(
    private val commandSender: CommandSender,
    private val argumentLabel: String,
    //                             argument, index    parameter
    private val parameterMap: Map<Pair<String, Int>, KParameter>
) : CommandContext {
    private data class ContextData(val contextKey: String, val index: Int)

    private val arguments: Map<ContextData, String> = parameterMap.map { (argument, kParameter) ->
        ContextData(getContextKey(kParameter), argument.second) to argument.first
    }.toMap()

    private val argumentsByType: Multimap<KClassifier, String> = ArrayListMultimap.create<KClassifier, String>().apply {
        parameterMap.forEach { (key, kParameter) ->
            this.put(kParameter.type.classifier, arguments[ContextData(getContextKey(kParameter), key.second)])
        }
    }

    private fun getContextKey(kParameter: KParameter): String {
        return kParameter.findAnnotation<ContextKey>()?.key ?: kParameter.name!!
    }

    override fun getArgumentLabel(): String {
        return argumentLabel
    }

    override fun findArgumentByIndex(index: Int): String {
        return arguments.entries.firstOrNull { it.key.index == index }?.value ?: throw IllegalArgumentException()
    }

    override fun getCommandSender(): CommandSender {
        return commandSender
    }

    override fun findArgument(key: String): String? {
        return arguments.entries.firstOrNull { it.key.contextKey == key }?.value
    }

    override fun getArgument(key: String): String {
        return arguments.entries.firstOrNull { it.key.contextKey == key }?.value ?: throw IllegalArgumentException()
    }

    override fun getArguments(): Collection<String> {
        return arguments.values
    }

    override fun getArgumentsByType(kClassifier: KClassifier): Collection<String> {
        return argumentsByType.get(kClassifier)
    }
}