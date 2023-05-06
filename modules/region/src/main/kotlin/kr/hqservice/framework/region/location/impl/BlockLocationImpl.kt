package kr.hqservice.framework.region.location.impl

import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.math.Point
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

data class BlockLocationImpl(
    private val world: World,
    private val vector3I: Point,
) : BlockLocation {
    constructor(world: World, x: Int, y: Int, z: Int) : this(world, Point(x, y, z))

    override fun getWorld(): World {
        return world
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
        return Location(world, getX() + .5, getY() + .5, getZ() + .5)
    }

    override fun getBlock(): Block {
        return toLocation().block
    }

    override fun toString(): String {
        return "BlockLocation=[world=${world.name}, x=${getX()}, y=${getY()}, z=${getZ()}]"
    }

    override fun clone(): BlockLocation {
        return copy()
    }
}