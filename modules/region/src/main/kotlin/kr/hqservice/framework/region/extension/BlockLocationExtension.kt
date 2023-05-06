package kr.hqservice.framework.region.extension

import kr.hqservice.framework.region.factory.RangeFactory
import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.range.Range
import org.bukkit.Location
import org.bukkit.entity.Player
import org.koin.java.KoinJavaComponent.getKoin

private val rangeFactory: RangeFactory by getKoin().inject()

fun Player.teleport(blockLocation: BlockLocation) {
    teleport(blockLocation.toLocation())
}

fun Location.asBlockLocation(): BlockLocation {
    return BlockLocationImpl(world!!, blockX, blockY, blockZ)
}

fun BlockLocation.toList(endPosition: BlockLocation): List<BlockLocation> {
    val mutableList = mutableListOf<BlockLocation>()
    val world = getWorld()
    for(x in getX() .. endPosition.getX())
        for(y in getY() .. endPosition.getY())
            for(z in getZ() .. endPosition.getZ())
                mutableList.add(BlockLocationImpl(world, x, y, z))
    return mutableList
}

operator fun BlockLocation.rangeTo(otherLocation: BlockLocation): Range {
    return rangeFactory.makeRange(this, otherLocation)
}