package kr.hqservice.framework.database.column

import kr.hqservice.framework.bukkit.core.extension.toItemStack
import kr.hqservice.framework.database.extension.toExposedBlob
import kr.hqservice.framework.database.extension.toItemStack
import kr.hqservice.framework.database.util.ExposedPropertyDelegate
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import kotlin.reflect.KProperty

fun Table.itemStack(name: String): Column<ExposedBlob> = blob(name)

fun Entity<*>.itemStack(column: Column<ExposedBlob>): ExposedPropertyDelegate<ItemStack> = object : ExposedPropertyDelegate<ItemStack> {
    override operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): ItemStack {
        val blob = entity.run { column.getValue(this, desc) }
        return blob.toItemStack()
    }

    override operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: ItemStack,
    ) {
        entity.apply { column.setValue(this, desc, value.toExposedBlob()) }
    }
}

@JvmName("itemStackNullable")
fun Entity<*>.itemStack(column: Column<ExposedBlob?>): ExposedPropertyDelegate<ItemStack?> = object : ExposedPropertyDelegate<ItemStack?> {
    override operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): ItemStack? {
        val blob = entity.run { column.getValue(this, desc) }
        return blob?.bytes?.toItemStack()
    }

    override operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: ItemStack?,
    ) {
        entity.apply { column.setValue(this, desc, value?.toExposedBlob()) }
    }
}