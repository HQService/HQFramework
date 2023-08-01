package kr.hqservice.framework.global.core.util

import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.jvm.isAccessible

fun Method.safeInvoke(obj: Any?, vararg args: Any?): Any? {
    if (!canAccess(obj)) {
        isAccessible = true
    }
    return invoke(obj, *args)
}

fun <T> KCallable<T>.safeCall(vararg args: Any?): T {
    if (!this.isAccessible) {
        isAccessible = true
    }
    return call(*args)
}