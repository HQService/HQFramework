package kr.hqservice.framework.database

import kr.hqservice.framework.database.dao.IntTimestampEntity
import kr.hqservice.framework.database.dao.IntTimestampEntityClass
import kr.hqservice.framework.database.dao.id.IntIdTimestampTable
import org.jetbrains.exposed.dao.id.EntityID

class TestDatabase {

}

class TestEntity(id: EntityID<Int>) : IntTimestampEntity(id, TestTable) {
    companion object : IntTimestampEntityClass<TestEntity>(TestTable)
    var test by TestTable.test
}

@Table
object TestTable : IntIdTimestampTable("hqtest_test") {
    val test = varchar("test", 20)
}