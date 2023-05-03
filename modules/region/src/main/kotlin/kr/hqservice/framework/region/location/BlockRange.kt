package kr.hqservice.framework.region.location

import org.bukkit.Location
import kotlin.math.abs

data class BlockRange(
    val minPosition: BlockPosition,
    val maxPosition: BlockPosition
) {
    override fun toString(): String {
        return "$minPosition~$maxPosition"
    }
}

operator fun BlockRange.contains(position: BlockPosition): Boolean {
    return position.x in minPosition.x .. maxPosition.x
            && position.y in minPosition.y .. maxPosition.y
            && position.z in minPosition.z .. maxPosition.z
}

operator fun BlockRange.contains(location: Location): Boolean {
    return location.toBlockPosition() in this
}

fun BlockRange.forEach(block: (BlockPosition) -> Unit) {
    for (x in minPosition.x..maxPosition.x) {
        for (y in minPosition.y..maxPosition.y) {
            for (z in minPosition.z..maxPosition.z) {
                block(BlockPosition(x, y, z))
            }
        }
    }
}