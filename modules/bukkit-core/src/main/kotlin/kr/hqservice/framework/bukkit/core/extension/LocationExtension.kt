package kr.hqservice.framework.bukkit.core.extension

import org.bukkit.Location
import org.bukkit.util.Vector

fun Location.getFrontLocation(distance: Double, absolute: Boolean = true): Location {
    val vec = direction
    if (absolute) vec.setY(0)
    return clone().add(vec.normalize().multiply(distance))
}

fun Location.getLeftLocation(distance: Double): Location {
    val direction = direction.normalize()
    val newDirection = Vector(direction.z, 0.0, -direction.x).normalize()
    return add(newDirection.multiply(distance))
}

fun Location.getRightLocation(distance: Double): Location {
    val direction = direction.normalize()
    val newDirection = Vector(-direction.z, 0.0, direction.x).normalize()
    return add(newDirection.multiply(distance))
}