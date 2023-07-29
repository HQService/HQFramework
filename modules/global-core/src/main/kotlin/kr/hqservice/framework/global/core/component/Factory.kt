package kr.hqservice.framework.global.core.component

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Factory(val binds: Array<KClass<*>> = [])

typealias HQFactory = Factory