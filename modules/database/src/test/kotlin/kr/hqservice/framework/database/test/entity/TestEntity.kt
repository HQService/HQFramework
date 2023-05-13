package kr.hqservice.framework.database.test.entity

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TestEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var testField by TestTable.testField
}