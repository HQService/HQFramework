package kr.hqservice.framework.nms.handler.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType

class CallableVersionHandler(
    private val version: Version,
    private val functionType: FunctionType
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

    override fun isChangedName(): Boolean {
        return false
    }

    override fun isMatched(targetClass: KClass<*>, callable: KCallable<*>): Boolean {
        return callable.name == name
                && callable.parameters
                    .map { it.type.classifier as? KClass<*>? } == functionType.getParameterClasses(targetClass)
    }
}