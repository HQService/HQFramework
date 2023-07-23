package kr.hqservice.framework.region.location.impl

import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.math.Point
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

data class LazyBlockLocation(
    private val worldName: String,
    private val vector3I: Point,
) : BlockLocation {
    constructor(worldName: String, x: Int, y: Int, z: Int) : this(worldName, Point(x, y, z))

    override fun getWorld(): World {
        return Bukkit.getWorld(worldName) ?: throw NullPointerException("$worldName 월드를 찾을 수 없습니다.")
    }

    override fun getX(): Int {
        return vector3I.x
    }

    override fun getY(): Int {
        return vector3I.y
    }

    override fun getZ(): Int {
        return vector3I.z
    }

    override fun getPoint(): Point {
        return vector3I
    }

    override fun toLocation(): Location {
        return Location(getWorld(), getX() + .5, getY() + .5, getZ() + .5)
    }

    override fun getBlock(): Block {
        return toLocation().block
    }

    override fun toString(): String {
        return "BlockLocation=[world=${worldName}, x=${getX()}, y=${getY()}, z=${getZ()}]"
    }

    override fun clone(): BlockLocation {
        return copy()
    }

    override fun hashCode(): Int {
        var result = worldName.hashCode()
        result = 31 * result + vector3I.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            !is BlockLocation -> false
            else -> hashCode() == other.hashCode()
        }
    }
}