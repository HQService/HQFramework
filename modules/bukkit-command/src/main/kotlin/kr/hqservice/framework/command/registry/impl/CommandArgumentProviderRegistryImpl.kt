package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.registry.CommandArgumentProviderRegistry
import kr.hqservice.framework.global.core.component.Bean
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

@Bean
class CommandArgumentProviderRegistryImpl : CommandArgumentProviderRegistry {
    private val arguments: MutableMap<String, CommandArgumentProvider<*>> = mutableMapOf()

    override fun addProvider(provider: CommandArgumentProvider<*>, qualifier: String?) {
        arguments[getArgumentProviderKey(getArgumentProviderType(provider), qualifier)] = provider
    }

    override fun findProvider(kClass: KClass<*>, qualifier: String?): CommandArgumentProvider<*>? {
        return arguments[getArgumentProviderKey(kClass, qualifier)]
    }

    override fun getProvider(kClass: KClass<*>, qualifier: String?): CommandArgumentProvider<*> {
        return arguments[getArgumentProviderKey(kClass, qualifier)]
            ?: throw IllegalArgumentException("argument provider with classifier $kClass, $qualifier not found.")
    }

    private fun getArgumentProviderType(argumentProvider: CommandArgumentProvider<*>): KClass<*> {
        return argumentProvider::class
            .supertypes
            .first { it.isSubtypeOf(CommandArgumentProvider::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure
    }

    private fun getArgumentProviderKey(argumentProviderClass: KClass<*>, qualifier: String?): String {
        val qualifiedTypeName = argumentProviderClass.qualifiedName
        val qualifierOrPrimary = qualifier ?: "primary$"
        return "${qualifiedTypeName}_\$_${qualifierOrPrimary}"
    }
}