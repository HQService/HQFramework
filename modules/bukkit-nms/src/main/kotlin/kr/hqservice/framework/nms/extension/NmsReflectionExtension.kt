package kr.hqservice.framework.nms.extension

import kotlin.reflect.KCallable
import kotlin.reflect.jvm.isAccessible

internal inline fun <reified R> KCallable<*>.callAccess(vararg instance: Any): R {
    return if (!isAccessible) {
        isAccessible = true
        val result = call(*instance) as R
        //isAccessible = false
        result
    } else call(*instance) as R
}