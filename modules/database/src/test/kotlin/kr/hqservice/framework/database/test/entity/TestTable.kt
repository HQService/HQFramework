package kr.hqservice.framework.database.test.entity

import org.jetbrains.exposed.dao.id.UUIDTable

object TestTable : UUIDTable("test_table") {
    val testField = integer("test_field")
}