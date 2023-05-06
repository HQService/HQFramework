package kr.hqservice.framework.region.range

import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.range.enums.LineAxis
import kr.hqservice.framework.region.range.enums.Offset

class LineRange(
    minPosition: BlockLocation,
    maxPosition: BlockLocation,
    val axis: LineAxis,
) : Range(minPosition, maxPosition) {
    fun getPoint(offset: Int): PointRange {
        val dot: Int = when (axis) {
            LineAxis.HORIZONTAL_Z ->
                getDot(minPosition.getX(), getCenter().getX(), maxPosition.getX(), offset)
            LineAxis.HORIZONTAL_X ->
                getDot(minPosition.getZ(), getCenter().getZ(), maxPosition.getZ(), offset)
            LineAxis.VERTICAL ->
                getDot(minPosition.getY(), getCenter().getY(), maxPosition.getY(), offset)
        }
        return PointRange(BlockLocationImpl(getWorld(), dot, dot, dot))
    }

    fun getPoint(offset: Offset): PointRange {
        val dot: Int = when (axis) {
            LineAxis.HORIZONTAL_Z ->
                getDot(minPosition.getX(), getCenter().getX(), maxPosition.getX(), -1, offset)
            LineAxis.HORIZONTAL_X ->
                getDot(minPosition.getZ(), getCenter().getZ(), maxPosition.getZ(), -1, offset)
            LineAxis.VERTICAL ->
                getDot(minPosition.getY(), getCenter().getY(), maxPosition.getY(), -1, offset)
        }
        return PointRange(BlockLocationImpl(getWorld(), dot, dot, dot))
    }
}