package kr.hqservice.framework.core.component

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class HQSingleton(val binds: Array<KClass<*>> = [])
