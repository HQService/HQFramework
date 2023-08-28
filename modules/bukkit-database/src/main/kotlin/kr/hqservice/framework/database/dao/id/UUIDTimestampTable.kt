package kr.hqservice.framework.database.dao.id

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import java.util.*

abstract class UUIDTimestampTable(name: String, columnName: String = "id") : TimestampIdTable<UUID>(name) {
    final override val id: Column<EntityID<UUID>> = uuid(columnName).autoGenerate().entityId()
    final override val primaryKey = PrimaryKey(id)
}