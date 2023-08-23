package kr.hqservice.framework.database.column

import kr.hqservice.framework.database.util.ExposedPropertyDelegate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import kotlin.reflect.KProperty

fun Entity<*>.location(column: Column<String>): ExposedPropertyDelegate<Location> = LocationExposedPropertyDelegate(column)

@JvmName("locationNullable")
fun Entity<*>.location(column: Column<String?>): ExposedPropertyDelegate<Location?> = LocationExposedPropertyDelegateNullable(column)

private class LocationExposedPropertyDelegate(
    val column: Column<String>,
) : ExposedPropertyDelegate<Location> {
    override operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): Location {
        val data = entity.run { column.getValue(this, desc) }
        val slices = data.split(";")
        return Location(
            Bukkit.getWorld(slices[0]),
            slices[1].toDouble(),
            slices[2].toDouble(),
            slices[3].toDouble(),
            slices[4].toFloat(),
            slices[5].toFloat(),
        )
    }

    override operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: Location,
    ) {
        val world = requireNotNull(value.world) { "Location with nullable world can't be stored in database" }
        val parsed = value.run { "${world.name};$x;$y;$z;$yaw;$pitch" }
        entity.apply { column.setValue(this, desc, parsed) }
    }
}

private class LocationExposedPropertyDelegateNullable(
    val column: Column<String?>,
) : ExposedPropertyDelegate<Location?> {
    override operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): Location? {
        val data = entity.run { column.getValue(this, desc) }
        val slices = data?.split(";")
        return slices?.let {
            Location(
                Bukkit.getWorld(it[0]),
                it[1].toDouble(),
                it[2].toDouble(),
                it[3].toDouble(),
                it[4].toFloat(),
                it[5].toFloat(),
            )
        }
    }

    override operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: Location?,
    ) {
        val parsed = value?.run {
            val world = requireNotNull(world) { "Location with nullable world can't be stored in database" }
            "${world.name};$x;$y;$z;$yaw;$pitch"
        }
        entity.apply { column.setValue(this, desc, parsed) }
    }
}