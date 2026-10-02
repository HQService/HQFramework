package kr.hqservice.framework.command.registry

import kr.hqservice.framework.command.CommandArgumentProvider
import kotlin.reflect.KClass

interface CommandArgumentProviderRegistry {
    fun addProvider(provider: CommandArgumentProvider<*>, qualifier: String?)

    fun getProvider(kClass: KClass<*>, qualifier: String?): CommandArgumentProvider<*>
}