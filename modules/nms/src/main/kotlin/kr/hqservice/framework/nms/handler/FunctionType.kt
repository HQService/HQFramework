package kr.hqservice.framework.nms.handler

import kotlin.reflect.KClass
import kotlin.reflect.KType

class FunctionType(
    private val name: String,
    private var returnType: KType? = null,
    private var parameterClasses: List<KClass<*>> = emptyList(),
    private var staticMethod: Boolean = false
) {
    fun setReturnType(clazz: KType?) {
        returnType = clazz
    }

    fun setParameterClasses(vararg classes: KClass<*>) {
        parameterClasses = classes.toList()
    }

    fun static() {
        staticMethod = true
    }

    fun isStaticMethod(): Boolean {
        return staticMethod
    }

    fun getName() = name

    fun getReturnType() = returnType

    fun getParameterClasses(targetClass: KClass<*>): List<KClass<*>> {
        return if (isStaticMethod())
            parameterClasses
        else listOf(targetClass, *parameterClasses.toTypedArray())
    }
}