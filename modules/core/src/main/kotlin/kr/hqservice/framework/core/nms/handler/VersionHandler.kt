package kr.hqservice.framework.core.nms.handler

import kr.hqservice.framework.core.nms.Version
import kotlin.reflect.KCallable

interface VersionHandler {

    fun getVersion(): Version

    fun getName(): String

    fun isMatched(callable: KCallable<*>): Boolean

}