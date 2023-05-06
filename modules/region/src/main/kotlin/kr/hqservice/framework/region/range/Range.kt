package kr.hqservice.framework.region.range

import kr.hqservice.framework.region.extension.asBlockLocation
import kr.hqservice.framework.region.extension.toList
import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.math.Point
import kr.hqservice.framework.region.range.enums.Offset
import org.bukkit.Location
import org.bukkit.World

sealed class Range(
    val minPosition: BlockLocation,
    val maxPosition: BlockLocation,
) : List<BlockLocation> by minPosition.toList(maxPosition) {
    fun getWorld(): World {
        return minPosition.getWorld()
    }

    fun contains(location: Location): Boolean {
        return contains(location.asBlockLocation())
    }

    override fun contains(element: BlockLocation): Boolean {
        return if (getWorld() != element.getWorld()) false
        else contains(element.getPoint())
    }

    private fun contains(point: Point): Boolean {
        return point.x in minPosition.getX() .. maxPosition.getX() &&
                point.y in minPosition.getY() .. maxPosition.getY() &&
                point.z in minPosition.getZ() .. maxPosition.getZ()
    }

    fun collidesWith(range: Range): Boolean {
        return if (this === range) true
        else contains(range.minPosition) || contains(range.maxPosition)
                || range.contains(minPosition) || range.contains(maxPosition)
    }

    open fun getCenter(): BlockLocation {
        val (x1, y1, z1) = minPosition.getPoint()
        val (x2, y2, z2) = maxPosition.getPoint()
        return BlockLocationImpl(minPosition.getWorld(), (x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2)
    }

    protected fun getDot(min: Int, center: Int, max: Int, offsetValue: Int, offset: Offset? = null): Int {
        val result = if(offset != null) when(offset) {
            Offset.CENTER -> center
            Offset.MAX -> max
            Offset.MIN -> min
        } else center + offsetValue
        if (result !in min..max)
            throw IllegalArgumentException("영역 밖의 offset 을 지정할 수 없습니다. (${min - 1} < offset < ${max + 1})")
        return result
    }
}
