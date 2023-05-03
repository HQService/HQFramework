package kr.hqservice.framework.core.nms.handler.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.handler.FunctionType
import kr.hqservice.framework.core.nms.handler.VersionHandler
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType

class CallableVersionHandler(
    private val version: Version,
    val functionType: FunctionType
) : VersionHandler {

    private val name: String = functionType.getName()
    private val returnType: KType? = functionType.getReturnType()

    override fun getVersion(): Version {
        return version
    }

    override fun getName(): String {
        return functionType.getName()
    }

    override fun static(): VersionHandler {
        functionType.static()
        return this
    }

    override fun isMatched(targetClass: KClass<*>, callable: KCallable<*>): Boolean {
        return callable.name == name
                && callable.parameters.map { it.type.classifier as KClass<*> } == functionType.getParameterClasses(targetClass)
    }

}