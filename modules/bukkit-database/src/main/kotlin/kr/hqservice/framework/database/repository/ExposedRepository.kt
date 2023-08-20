package kr.hqservice.framework.database.repository

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.SizedIterable

interface ExposedRepository<ID : Comparable<ID>, E : Entity<ID>> {
    fun count(): Long

    fun save(entity: E.() -> Unit): E

    fun delete(entity: E)

    fun deleteById(id: ID)

    fun existsById(id: ID)

    fun findAll(): SizedIterable<E>

    fun findById(id: ID): E?
}