package kr.hqservice.framework.database.dao.id

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

abstract class IntIdTimestampTable(name: String, columnName: String = "id") : TimestampIdTable<Int>(name) {
    final override val id: Column<EntityID<Int>> = integer(columnName).autoIncrement().entityId()
    final override val primaryKey = PrimaryKey(id)
}