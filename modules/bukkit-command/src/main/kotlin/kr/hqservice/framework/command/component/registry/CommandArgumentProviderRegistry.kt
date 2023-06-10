package kr.hqservice.framework.command.component.registry

import kr.hqservice.framework.command.component.HQCommandArgumentProvider
import kotlin.reflect.KClassifier

interface CommandArgumentProviderRegistry {
    fun addProvider(provider: HQCommandArgumentProvider<*>)

    fun findProvider(classifier: KClassifier): HQCommandArgumentProvider<*>?

    fun getProvider(classifier: KClassifier): HQCommandArgumentProvider<*>
}