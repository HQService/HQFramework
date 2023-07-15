package kr.hqservice.framework.inventory

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

fun view(named: String, parameters: ParametersDefinition? = null): HQView {
    return KoinJavaComponent.getKoin().get(named(named), parameters = parameters)
}