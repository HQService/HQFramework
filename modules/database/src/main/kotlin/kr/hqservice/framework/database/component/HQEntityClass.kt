package kr.hqservice.framework.database.component

import kr.hqservice.framework.core.component.HQComponent
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.IdTable

abstract class HQEntityClass<ID : Comparable<ID>, out T : Entity<ID>>(table: IdTable<ID>, entityType: Class<T>) : EntityClass<ID, T>(table, entityType), HQComponent