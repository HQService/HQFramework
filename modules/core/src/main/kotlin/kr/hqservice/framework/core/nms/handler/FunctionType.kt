package kr.hqservice.framework.core.nms.handler

import kotlin.reflect.KClass
import kotlin.reflect.KType

class FunctionType(
    private val name: String,
    private var returnType: KType? = null,
    private var parameterClasses: List<KClass<*>> = emptyList()
) {

    fun setReturnType(clazz: KType?) {
        returnType = clazz
    }

    fun setParameterClasses(vararg classes: KClass<*>) {
        parameterClasses = classes.toList()
    }

    fun getName() = name

    fun getReturnType() = returnType

    fun getParameterClasses() = parameterClasses

}