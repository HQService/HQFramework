package kr.hqservice.framework.nms.extension

import kotlin.reflect.KCallable
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

internal inline fun <reified R> KCallable<*>.callAccess(vararg instance: Any): R {
    val javaField = (this as KProperty).javaField!!
    return if (!javaField.canAccess(instance.first())) {
        javaField.isAccessible = true
        val result = javaField.get(instance.first()) as R
        javaField.isAccessible = false
        result
    } else call(*instance) as R
}

internal inline fun <reified R> KCallable<*>.setAccess(instance: Any, value: R) {
    val javaField = (this as KProperty).javaField!!
    if (!javaField.canAccess(instance)) {
        javaField.isAccessible = true
        javaField.set(instance, value)
        javaField.isAccessible = false
    } else javaField.set(instance, value)
}