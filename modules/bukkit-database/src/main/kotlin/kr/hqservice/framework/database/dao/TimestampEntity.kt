package kr.hqservice.framework.database.dao

import kr.hqservice.framework.database.dao.id.TimestampIdTable
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

abstract class TimestampEntity<ID : Comparable<ID>>(id: EntityID<ID>, table: TimestampIdTable<ID>) : Entity<ID>(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class TimestampEntityClass<ID : Comparable<ID>, E : TimestampEntity<ID>>(
    table: TimestampIdTable<ID>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<ID>) -> E)? = null,
) : EntityClass<ID, E>(table, entityType, entityCtor) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                action.toEntity(this)?.updatedAt = LocalDateTime.now()
            }
        }
    }
}