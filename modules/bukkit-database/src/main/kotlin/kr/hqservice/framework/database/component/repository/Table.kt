package kr.hqservice.framework.database.component.repository

import org.jetbrains.exposed.sql.Table
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Table(val with: Array<KClass<out Table>>)