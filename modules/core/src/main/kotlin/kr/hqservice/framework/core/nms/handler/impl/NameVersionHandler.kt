package kr.hqservice.framework.core.nms.handler.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.handler.VersionHandler
import kotlin.reflect.KCallable

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

    override fun isMatched(callable: KCallable<*>): Boolean {
        return callable.name == name
    }

}