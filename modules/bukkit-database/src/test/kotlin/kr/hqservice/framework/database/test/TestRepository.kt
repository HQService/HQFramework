package kr.hqservice.framework.database.test

import kr.hqservice.framework.database.component.datasource.HQDataSource
import kr.hqservice.framework.database.component.repository.HQRepository
import kr.hqservice.framework.database.test.entity.TestEntity
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.global.core.component.registry.MutableNamed
import kr.hqservice.framework.test.Isolated
import java.util.*

@Isolated("RepositoryTest")
@Singleton(binds = [TestRepository::class])
@Component
class TestRepositoryImpl(
    @MutableNamed("data-source-type") private val dataSource: HQDataSource
) : TestRepository {
    override suspend fun create(id: UUID, init: TestEntity.() -> Unit): TestEntity {
        return dataSource.query {
            TestEntity.new(id, init)
        }
    }

    override suspend fun get(id: UUID): TestEntity {
        return dataSource.query {
            TestEntity[id]
        }
    }

    override suspend fun find(id: UUID): TestEntity? {
        return dataSource.query {
            TestEntity.findById(id)
        }
    }

    override suspend fun update(entity: TestEntity, scope: TestEntity.() -> Unit): TestEntity {
        return dataSource.query {
            entity.scope()
            entity.flush()
            entity
        }
    }

    override suspend fun updateById(id: UUID, scope: TestEntity.() -> Unit): TestEntity {
        return dataSource.query {
            val entity = get(id)
            entity.scope()
            entity.flush()
            entity
        }
    }

    override suspend fun delete(id: UUID) {
        dataSource.query {
            TestEntity[id].delete()
        }
    }

    override suspend fun count(): Long {
        return dataSource.query {
            TestEntity.count()
        }
    }

    override fun getDataSource(): HQDataSource {
        return dataSource
    }
}

interface TestRepository : HQRepository {
    suspend fun create(id: UUID, init: TestEntity.() -> Unit): TestEntity

    suspend fun get(id: UUID): TestEntity

    suspend fun find(id: UUID): TestEntity?

    suspend fun update(entity: TestEntity, scope: TestEntity.() -> Unit): TestEntity

    suspend fun updateById(id: UUID, scope: TestEntity.() -> Unit): TestEntity

    suspend fun delete(id: UUID)

    suspend fun count(): Long
}