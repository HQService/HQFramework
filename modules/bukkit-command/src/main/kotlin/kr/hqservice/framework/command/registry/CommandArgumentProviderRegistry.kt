package kr.hqservice.framework.command.registry

import kr.hqservice.framework.command.CommandArgumentProvider
import kotlin.reflect.KClassifier

interface CommandArgumentProviderRegistry {
    fun addProvider(provider: CommandArgumentProvider<*>)

    fun findProvider(classifier: KClassifier): CommandArgumentProvider<*>?

    fun getProvider(classifier: KClassifier): CommandArgumentProvider<*>
}