package kr.hqservice.framework.command.component.registry.impl

import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kr.hqservice.framework.command.component.registry.CommandArgumentProviderRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClassifier
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

@Single(binds = [CommandArgumentProviderRegistry::class])
class CommandArgumentProviderRegistryImpl : CommandArgumentProviderRegistry {
    private val arguments: MutableMap<KClassifier, HQCommandArgumentProvider<*>> = mutableMapOf()

    override fun addProvider(provider: HQCommandArgumentProvider<*>) {
        arguments[getArgumentProviderType(provider)] = provider
    }

    override fun findProvider(classifier: KClassifier): HQCommandArgumentProvider<*>? {
        return arguments[classifier]
    }

    override fun getProvider(classifier: KClassifier): HQCommandArgumentProvider<*> {
        return arguments[classifier] ?: throw IllegalArgumentException("argument provider with classifier $classifier not found.")
    }

    private fun getArgumentProviderType(argumentProvider: HQCommandArgumentProvider<*>): KClassifier {
        return argumentProvider::class
            .supertypes
            .first { it.isSubtypeOf(HQCommandArgumentProvider::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure
    }
}