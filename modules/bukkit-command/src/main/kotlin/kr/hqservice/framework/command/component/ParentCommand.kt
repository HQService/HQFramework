package kr.hqservice.framework.command.component

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ParentCommand(val binds: Array<KClass<out HQCommandTree>>)