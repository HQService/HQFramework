package kr.hqservice.framework.region.location

import org.bukkit.Location
import org.bukkit.World

data class RegionCenter(
    val world: RegionWorld,
    val blockPosition: BlockPosition
) {

    constructor(world: RegionWorld, x: Int, y: Int, z: Int): this(world, BlockPosition(x, y, z))
    constructor(world: World, x: Int, y: Int, z: Int): this(RegionWorld(world), BlockPosition(x, y, z))
    constructor(location: Location): this(location.toRegionWorld(), BlockPosition(location.blockX, location.blockY, location.blockZ))

    private val x get() = blockPosition.x
    private val y get() = blockPosition.y
    private val z get() = blockPosition.z

    fun getRange(radius: Int, height: Int): BlockRange {
        val minPos = BlockPosition(x - radius, y - 1, z - radius)
        val maxPos = BlockPosition(x + radius, y + height, z + radius)
        return minPos .. maxPos
    }

    override fun toString(): String {
        return "$world,$blockPosition"
    }
}

fun Location.toRegionCenter(): RegionCenter {
    return RegionCenter(this)
}
