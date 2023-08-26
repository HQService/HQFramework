package kr.hqservice.framework.command

import kr.hqservice.framework.global.core.component.Scannable
import kotlin.reflect.KClass

@Scannable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val parent: KClass<*> = Any::class,
    val label: String = "",
    val priority: Int = 999,
    val permission: String = "",
    val isOp: Boolean = false
)