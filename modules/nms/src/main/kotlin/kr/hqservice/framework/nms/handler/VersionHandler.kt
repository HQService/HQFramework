package kr.hqservice.framework.nms.handler

import kr.hqservice.framework.nms.Version
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

interface VersionHandler {
    fun getVersion(): Version

    fun getName(): String

    fun static(): VersionHandler

    fun isChangedName(): Boolean

    fun isMatched(targetClass: KClass<*>, callable: KCallable<*>): Boolean
}