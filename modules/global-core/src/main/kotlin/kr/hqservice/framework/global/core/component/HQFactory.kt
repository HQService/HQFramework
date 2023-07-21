package kr.hqservice.framework.global.core.component

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class HQFactory(val binds: Array<KClass<*>> = [])

typealias Factory = HQFactory