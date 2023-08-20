package kr.hqservice.framework.database.dao

import kr.hqservice.framework.database.dao.id.TimestampIdTable
import kr.hqservice.framework.database.dao.id.UUIDTimestampTable
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

abstract class UUIDTimestampEntity(id: EntityID<UUID>, table: UUIDTimestampTable) : TimestampEntity<UUID>(id, table)

abstract class UUIDTimestampEntityClass<E : UUIDTimestampEntity>(
    table: TimestampIdTable<UUID>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<UUID>) -> E)? = null,
) : TimestampEntityClass<UUID, E>(table, entityType, entityCtor)

