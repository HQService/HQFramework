package kr.hqservice.framework.database.test.entity

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.database.component.HQEntityClass
import kr.hqservice.framework.test.Isolated
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Named
import java.util.*

@Named("test")
@Isolated("RepositoryTest")
@HQSingleton(binds = [HQEntityClass::class])
@Component
class TestEntityClass : HQEntityClass<UUID, TestEntity>(TestTable, TestEntity::class.java) {
    override fun findById(id: EntityID<UUID>): TestEntity? {
        return super.findById(id).also { println("findById Call") }
    }
}