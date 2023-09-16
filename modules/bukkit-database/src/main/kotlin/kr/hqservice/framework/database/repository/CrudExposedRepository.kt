package kr.hqservice.framework.database.repository

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

abstract class CrudExposedRepository<ID : Comparable<ID>, E : Entity<ID>>(private val entityClass: EntityClass<ID, E>) : ExposedRepository<ID, E> {
    override suspend fun findAll(): SizedIterable<E> {
        return newSuspendedTransaction {
            entityClass.all()
        }
    }

    override suspend fun new(entity: E.() -> Unit): E {
        return newSuspendedTransaction {
            entityClass.new(entity)
        }
    }

    override suspend fun findById(id: ID): E? {
        return newSuspendedTransaction {
            entityClass.findById(id)
        }
    }

    override suspend fun count(): Long {
        return newSuspendedTransaction {
            entityClass.count()
        }
    }

    override suspend fun deleteById(id: ID) {
        return newSuspendedTransaction {
            entityClass.findById(id)?.delete()
        }
    }

    override suspend fun delete(entity: E) {
        return newSuspendedTransaction {
            entity.delete()
        }
    }

    override suspend fun existsById(id: ID): Boolean {
        return newSuspendedTransaction {
            entityClass.findById(id) != null
        }
    }
}