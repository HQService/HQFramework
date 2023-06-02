package kr.hqservice.framework.database.test

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.database.component.HQDataSource
import kr.hqservice.framework.database.component.HQEntityClass
import kr.hqservice.framework.database.component.HQRepository
import kr.hqservice.framework.database.test.entity.TestEntity
import kr.hqservice.framework.global.core.component.registry.MutableNamed
import kr.hqservice.framework.test.Isolated
import org.koin.core.annotation.Named
import java.util.*

@Isolated("RepositoryTest")
@HQSingleton(binds = [TestRepository::class])
@Component
class TestRepositoryImpl(
    @MutableNamed("data-source-type") private val dataSource: HQDataSource,
    @Named("test") private val entityClass: HQEntityClass<UUID, TestEntity>,
) : TestRepository {
    override suspend fun create(id: UUID, init: TestEntity.() -> Unit): TestEntity {
        return dataSource.query {
            entityClass.new(id, init)
        }
    }

    override suspend fun get(id: UUID): TestEntity {
        return dataSource.query {
            entityClass[id]
        }
    }

    override suspend fun find(id: UUID): TestEntity? {
        return dataSource.query {
            entityClass.findById(id)
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
            entityClass[id].delete()
        }
    }

    override suspend fun count(): Long {
        return dataSource.query {
            entityClass.count()
        }
    }

    override fun getEntityClass(): HQEntityClass<UUID, TestEntity> {
        return entityClass
    }

    override fun getDataSource(): HQDataSource {
        return dataSource
    }
}

interface TestRepository : HQRepository<UUID, TestEntity> {
    suspend fun create(id: UUID, init: TestEntity.() -> Unit): TestEntity

    suspend fun get(id: UUID): TestEntity

    suspend fun find(id: UUID): TestEntity?

    suspend fun update(entity: TestEntity, scope: TestEntity.() -> Unit): TestEntity

    suspend fun updateById(id: UUID, scope: TestEntity.() -> Unit): TestEntity

    suspend fun delete(id: UUID)

    suspend fun count(): Long
}