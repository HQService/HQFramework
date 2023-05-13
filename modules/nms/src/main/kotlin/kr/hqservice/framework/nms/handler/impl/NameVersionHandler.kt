package kr.hqservice.framework.nms.handler.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.VersionHandler
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

class NameVersionHandler(
    private val version: Version,
    private val name: String,
    private val changedName: Boolean = false
) : VersionHandler {
    override fun getVersion(): Version {
        return version
    }

    override fun getName(): String {
        return name
    }

    override fun isChangedName(): Boolean {
        return changedName
    }

    override fun static(): VersionHandler {
        return this
    }

    override fun isMatched(targetClass: KClass<*>, callable: KCallable<*>): Boolean {
        return callable.name == name
                && callable.parameters.map { it.type.classifier as KClass<*> } == listOf(targetClass)
    }
}