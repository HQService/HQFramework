package kr.hqservice.framework.command.component.registry

import kr.hqservice.framework.command.component.CommandArgumentProvider
import kotlin.reflect.KClassifier

interface CommandArgumentProviderRegistry {
    fun addProvider(provider: CommandArgumentProvider<*>)

    fun findProvider(classifier: KClassifier): CommandArgumentProvider<*>?

    fun getProvider(classifier: KClassifier): CommandArgumentProvider<*>
}