package kr.hqservice.framework.database.dao.id

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.time.LocalDateTime

abstract class TimestampIdTable<ID : Comparable<ID>>(name: String) : IdTable<ID>(name) {
    abstract val createdAt: Column<LocalDateTime>
    abstract val updatedAt: Column<LocalDateTime?>
}