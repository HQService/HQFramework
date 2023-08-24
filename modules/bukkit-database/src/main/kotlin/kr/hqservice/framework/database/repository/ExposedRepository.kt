package kr.hqservice.framework.database.repository

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.SizedIterable

interface ExposedRepository<ID : Comparable<ID>, E : Entity<ID>> {
    suspend fun count(): Long

    suspend fun new(entity: E.() -> Unit): E

    suspend fun delete(entity: E)

    suspend fun deleteById(id: ID)

    suspend fun existsById(id: ID): Boolean

    suspend fun findAll(): SizedIterable<E>

    suspend fun findById(id: ID): E?
}