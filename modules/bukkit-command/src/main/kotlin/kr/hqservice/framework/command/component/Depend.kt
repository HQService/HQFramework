package kr.hqservice.framework.command.component

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Depend(val depends: Array<KClass<out HQCommandTree>> = [])