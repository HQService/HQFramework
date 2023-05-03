package kr.hqservice.framework.core.nms

import kr.hqservice.framework.core.nms.handler.FunctionType
import kr.hqservice.framework.core.nms.handler.VersionHandler
import kr.hqservice.framework.core.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.core.nms.handler.impl.NameVersionHandler

enum class Version {

    V_7,
    V_8,
    V_9,
    V_10,
    V_11,
    V_12,
    V_13,
    V_14,
    V_15,
    V_16,
    V_17,
    V_18,
    V_19;

    fun support(version: Version): Boolean = ordinal <= version.ordinal

    fun handle(name: String): VersionHandler {
        return NameVersionHandler(this, name)
    }

    fun handle(name: String, block: FunctionType.()->Unit): VersionHandler {
        if(name.isEmpty()) throw IllegalArgumentException("method without name")

        val type = FunctionType(name)
        block(type)
        return CallableVersionHandler(this, type)
    }

}