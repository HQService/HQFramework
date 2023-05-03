package kr.hqservice.framework.core.nms.handler

import kr.hqservice.framework.core.nms.Version
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

interface VersionHandler {

    fun getVersion(): Version

    fun getName(): String

    fun isMatched(targetClass: KClass<*>, callable: KCallable<*>): Boolean

}