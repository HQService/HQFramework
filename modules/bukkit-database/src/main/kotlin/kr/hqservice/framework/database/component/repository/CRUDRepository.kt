package kr.hqservice.framework.database.component.repository

interface CRUDRepository<ID : Comparable<ID>, E> : HQRepository {
    suspend fun create(id: ID, init: E.() -> Unit = {}): E

    suspend fun get(id: ID): E

    suspend fun find(id: ID): E?

    suspend fun update(entity: E, scope: E.() -> Unit): E

    suspend fun updateById(id: ID, scope: E.() -> Unit): E

    suspend fun delete(id: ID)

    suspend fun exists(id: ID): Boolean

    suspend fun count(): Long
}