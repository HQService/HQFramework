package kr.hqservice.framework.database.util

import org.jetbrains.exposed.dao.Entity
import kotlin.reflect.KProperty

interface ExposedPropertyDelegate<T> {
    operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): T

    operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: T,
    )
}