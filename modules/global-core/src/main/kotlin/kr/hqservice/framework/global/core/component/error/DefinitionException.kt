package kr.hqservice.framework.global.core.component.error

import kr.hqservice.framework.global.core.extension.toHumanReadable
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

abstract class DefinitionException(private val detailMessage: String, val classes: List<KClass<*>>) : Exception() {
    override val message: String
        get() = "$detailMessage: " +
                classes.map { "\n$it [${getDetailedParameterInfoMessage(it)}]" }.toString().replace("[", "").replace("]", "") + "\n" +
                ""

    private fun getDetailedParameterInfoMessage(kClass: KClass<*>): String {
        val parameters = kClass.primaryConstructor?.valueParameters ?: return "(primary constructor not found)"
        val parameterDisplays = mutableListOf<String>()
        parameters.forEach {
            parameterDisplays.add(it.type.jvmErasure.simpleName ?: "simple name is null")
        }
        return "(${parameterDisplays.toHumanReadable()})"
    }
}