package kr.hqservice.framework.database.dao.id

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

abstract class UUIDTimestampTable(name: String, columnName: String = "id") : TimestampIdTable<UUID>(name) {
    final override val id: Column<EntityID<UUID>> = uuid(columnName).autoGenerate().entityId()
    final override val primaryKey = PrimaryKey(id)
    final override val createdAt = datetime("createdAt").clientDefault { LocalDateTime.now() }
    final override val updatedAt = datetime("updatedAt").nullable()
}