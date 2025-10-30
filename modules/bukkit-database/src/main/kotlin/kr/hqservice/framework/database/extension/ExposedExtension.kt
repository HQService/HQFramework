package kr.hqservice.framework.database.extension

import kr.hqservice.framework.bukkit.core.extension.toByteArray
import kr.hqservice.framework.bukkit.core.extension.toItemArray
import kr.hqservice.framework.bukkit.core.extension.toItemStack
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.DaoEntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.vendors.ForUpdateOption

fun ByteArray.toExposedBlob(): ExposedBlob {
    return ExposedBlob(this)
}

fun ItemStack.toExposedBlob(): ExposedBlob {
    return this.toByteArray().toExposedBlob()
}

fun Array<ItemStack?>.toExposedBlob(): ExposedBlob {
    return this.toByteArray().toExposedBlob()
}

fun ExposedBlob.toItemStack(): ItemStack {
    return this.inputStream.use {
        it.readAllBytes().toItemStack()
    }
}

fun ExposedBlob.toItemArray(): Array<ItemStack> {
    return this.inputStream.use { stream ->
        stream.readAllBytes().toItemArray()
    }
}

fun <ID : Comparable<ID>, E : Entity<ID>> EntityClass<ID, E>.findForUpdate(
    forUpdateOption: ForUpdateOption = ForUpdateOption.ForUpdate,
    op: SqlExpressionBuilder.() -> Op<Boolean>,
): SizedIterable<E> {
    val query = searchQuery(Op.build(op)).forUpdate(forUpdateOption)
    return wrapRows(query)
}

fun <ID : Comparable<ID>, E : Entity<ID>> EntityClass<ID, E>.findByIdForUpdate(
    id: EntityID<ID>
): E? {
    return this.testCache(id) ?: findForUpdate { this@findByIdForUpdate.table.id eq id }.firstOrNull()
}

fun <ID : Comparable<ID>, E : Entity<ID>> EntityClass<ID, E>.findByIdForUpdate(
    id: ID
): E? {
    return findByIdForUpdate(DaoEntityID(id, this.table))
}

fun <ID : Comparable<ID>, E : Entity<ID>> EntityClass<ID, E>.getForUpdate(
    id: EntityID<ID>
): E {
    return findByIdForUpdate(id) ?: throw EntityNotFoundException(id, this)
}

fun <ID : Comparable<ID>, E : Entity<ID>> EntityClass<ID, E>.getForUpdate(
    id: ID
): E {
    return getForUpdate(DaoEntityID(id, this.table))
}