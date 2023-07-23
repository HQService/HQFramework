package kr.hqservice.framework.region.range

import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.range.enums.Offset
import kr.hqservice.framework.region.range.enums.PlaneAxis

class DimensionRange(
    minPosition: BlockLocation,
    maxPosition: BlockLocation
) : Range(minPosition, maxPosition) {
    fun getPlaneRange(axis: PlaneAxis, offset: Offset): PlaneRange {
        return getPlaneRange(-1, axis, offset)
    }

    fun getPlaneRange(axis: PlaneAxis, offset: Int): PlaneRange {
        return getPlaneRange(offset, axis, null)
    }

    private fun getPlaneRange(offsetValue: Int, targetAxis: PlaneAxis, offset: Offset? = null): PlaneRange {
        val world = getWorld()
        return when (targetAxis) {
            PlaneAxis.HORIZONTAL -> {
                val y = getDot(minPosition.getY(), getCenter().getY(), maxPosition.getY(), offsetValue, offset)
                PlaneRange(
                    BlockLocationImpl(world, minPosition.getX(), y, minPosition.getZ()),
                    BlockLocationImpl(world, maxPosition.getX(), y, maxPosition.getZ()),
                    targetAxis
                )
            }

            PlaneAxis.VERTICAL_X -> {
                val x = getDot(minPosition.getX(), getCenter().getX(), maxPosition.getX(), offsetValue, offset)
                PlaneRange(
                    BlockLocationImpl(world, x, minPosition.getY(), minPosition.getZ()),
                    BlockLocationImpl(world, x, maxPosition.getY(), maxPosition.getZ()),
                    targetAxis
                )
            }

            PlaneAxis.VERTICAL_Z -> {
                val z = getDot(minPosition.getZ(), getCenter().getZ(), maxPosition.getZ(), offsetValue, offset)
                PlaneRange(
                    BlockLocationImpl(world, minPosition.getX(), minPosition.getY(), z),
                    BlockLocationImpl(world, maxPosition.getX(), maxPosition.getY(), z),
                    targetAxis
                )
            }
        }
    }
}