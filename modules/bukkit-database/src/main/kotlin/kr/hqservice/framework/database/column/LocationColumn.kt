package kr.hqservice.framework.database.column

import kr.hqservice.framework.database.util.ExposedPropertyDelegate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import kotlin.reflect.KProperty

fun Table.location(name: String): Column<String> = varchar(name, 255)

fun Entity<*>.location(column: Column<String>): ExposedPropertyDelegate<Location> = object : ExposedPropertyDelegate<Location> {
    override operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): Location {
        val data = entity.run { column.getValue(this, desc) }
        return parseLocation(data)
    }

    override operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: Location,
    ) {
        entity.apply { column.setValue(this, desc, serializeLocation(value)) }
    }
}

@JvmName("locationNullable")
fun Entity<*>.location(column: Column<String?>): ExposedPropertyDelegate<Location?> = object : ExposedPropertyDelegate<Location?> {
    override operator fun <ID : Comparable<ID>> getValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
    ): Location? {
        val data = entity.run { column.getValue(this, desc) }
        return data?.let(::parseLocation)
    }

    override operator fun <ID : Comparable<ID>> setValue(
        entity: Entity<ID>,
        desc: KProperty<*>,
        value: Location?,
    ) {
        entity.apply { column.setValue(this, desc, value?.let(::serializeLocation)) }
    }
}

internal fun serializeLocation(location: Location): String {
    val world = requireNotNull(location.world) { "Location with nullable world can't be stored in database" }
    return location.run { "$x;$y;$z;$yaw;$pitch;${world.name}" }
}

internal fun parseLocation(data: String): Location {
    if (data.substringBefore(";").toDoubleOrNull() == null) return parseLegacyLocation(data)
    val slices = data.split(";", limit = 6)
    return Location(
        Bukkit.getWorld(slices[5]),
        slices[0].toDouble(),
        slices[1].toDouble(),
        slices[2].toDouble(),
        slices[3].toFloat(),
        slices[4].toFloat(),
    )
}

private fun parseLegacyLocation(data: String): Location {
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