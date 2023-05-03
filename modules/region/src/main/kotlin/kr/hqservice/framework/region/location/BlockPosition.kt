package kr.hqservice.framework.region.location

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import kotlin.math.max
import kotlin.math.min

data class BlockPosition(
    val x: Int,
    val y: Int,
    val z: Int
) {

    fun getBlock(world: World): Block {
        return world.getBlockAt(x, y, z)
    }

    fun getLocation(world: World): Location {
        return getBlock(world).location
    }

    override fun toString(): String {
        return "$x,$y,$z"
    }
}

fun Location.toBlockPosition(): BlockPosition {
    return BlockPosition(blockX, blockY, blockZ)
}

operator fun Location.rangeTo(other: Location): BlockRange {
    return this.toBlockPosition() .. other.toBlockPosition()
}

operator fun BlockPosition.rangeTo(other: BlockPosition): BlockRange {
    return BlockRange(
        BlockPosition(min(x, other.x), min(y, other.y), min(z, other.z)),
        BlockPosition(max(x, other.x), max(y, other.y), max(z, other.z))
    )
}
