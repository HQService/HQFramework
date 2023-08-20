package kr.hqservice.framework.database.dao

import kr.hqservice.framework.database.dao.id.LongIdTimestampTable
import kr.hqservice.framework.database.dao.id.TimestampIdTable
import org.jetbrains.exposed.dao.id.EntityID

abstract class LongTimestampEntity(id: EntityID<Long>, table: LongIdTimestampTable) : TimestampEntity<Long>(id, table)

abstract class LongTimestampEntityClass<E : LongTimestampEntity>(
    table: TimestampIdTable<Long>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<Long>) -> E)? = null,
) : TimestampEntityClass<Long, E>(table, entityType, entityCtor)

