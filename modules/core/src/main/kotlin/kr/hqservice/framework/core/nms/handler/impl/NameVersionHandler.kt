package kr.hqservice.framework.core.nms.handler.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.handler.VersionHandler
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

class NameVersionHandler(
    private val version: Version,
    private val name: String
) : VersionHandler {

    override fun getVersion(): Version {
        return version
    }

    override fun getName(): String {
        return name
    }

    override fun static(): VersionHandler {
        return this
    }

    override fun isMatched(targetClass: KClass<*>, callable: KCallable<*>): Boolean {
        return callable.name == name
    }

}