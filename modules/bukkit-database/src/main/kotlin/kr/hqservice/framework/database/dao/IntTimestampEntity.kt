package kr.hqservice.framework.database.dao

import kr.hqservice.framework.database.dao.id.IntIdTimestampTable
import kr.hqservice.framework.database.dao.id.TimestampIdTable
import org.jetbrains.exposed.dao.id.EntityID

abstract class IntTimestampEntity(id: EntityID<Int>, table: IntIdTimestampTable) : TimestampEntity<Int>(id, table)

abstract class IntTimestampEntityClass<E : IntTimestampEntity>(
    table: TimestampIdTable<Int>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<Int>) -> E)? = null,
) : TimestampEntityClass<Int, E>(table, entityType, entityCtor)