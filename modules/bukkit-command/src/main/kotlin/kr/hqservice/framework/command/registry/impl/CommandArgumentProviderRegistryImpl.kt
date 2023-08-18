package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.global.core.component.Bean
import kotlin.reflect.KClassifier
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

@Bean
class CommandArgumentProviderRegistryImpl : CommandArgumentProviderRegistry {
    private val arguments: MutableMap<KClassifier, CommandArgumentProvider<*>> = mutableMapOf()

    override fun addProvider(provider: CommandArgumentProvider<*>) {
        arguments[getArgumentProviderType(provider)] = provider
    }

    override fun findProvider(classifier: KClassifier): CommandArgumentProvider<*>? {
        return arguments[classifier]
    }

    override fun getProvider(classifier: KClassifier): CommandArgumentProvider<*> {
        return arguments[classifier]
            ?: throw IllegalArgumentException("argument provider with classifier $classifier not found.")
    }

    private fun getArgumentProviderType(argumentProvider: CommandArgumentProvider<*>): KClassifier {
        return argumentProvider::class
            .supertypes
            .first { it.isSubtypeOf(CommandArgumentProvider::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure
    }
}