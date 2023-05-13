package kr.hqservice.framework.database.test.entity

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.Singleton
import kr.hqservice.framework.database.component.HQEntityClass
import kr.hqservice.framework.test.Isolated
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Named
import java.util.*

@Named("test")
@Isolated("RepositoryTest")
@Singleton(binds = [HQEntityClass::class])
@Component
class TestEntityClass : HQEntityClass<UUID, TestEntity>(TestTable, TestEntity::class.java) {
    override fun findById(id: EntityID<UUID>): TestEntity? {
        return super.findById(id).also { println("findById Call") }
    }
}