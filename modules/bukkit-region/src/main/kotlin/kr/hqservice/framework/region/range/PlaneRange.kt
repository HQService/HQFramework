package kr.hqservice.framework.region.range

import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.range.enums.LineAxis
import kr.hqservice.framework.region.range.enums.Offset
import kr.hqservice.framework.region.range.enums.PlaneAxis

class PlaneRange(
    minPosition: BlockLocation,
    maxPosition: BlockLocation,
    val axis: PlaneAxis,
) : Range(minPosition, maxPosition) {
    fun getLineRange(axis: LineAxis, offset: Offset): LineRange {
        return getLineRange(-1, axis, offset)
    }

    fun getLineRange(axis: LineAxis, offset: Int): LineRange {
        return getLineRange(offset, axis, null)
    }

    private fun getLineRange(offsetValue: Int, targetAxis: LineAxis, offset: Offset? = null): LineRange {
        val world = getWorld()
        return when (axis) {
            PlaneAxis.HORIZONTAL -> {
                when (targetAxis) {
                    LineAxis.HORIZONTAL_X -> {
                        val x = getDot(
                            minPosition.getX(),
                            (minPosition.getX() + maxPosition.getX()) / 2,
                            maxPosition.getX(),
                            offsetValue,
                            offset
                        )
                        LineRange(
                            BlockLocationImpl(world, x, minPosition.getY(), minPosition.getZ()),
                            BlockLocationImpl(world, x, maxPosition.getY(), maxPosition.getZ()),
                            targetAxis
                        )
                    }

                    LineAxis.HORIZONTAL_Z -> {
                        val z = getDot(
                            minPosition.getZ(),
                            (minPosition.getZ() + maxPosition.getZ()) / 2,
                            maxPosition.getZ(),
                            offsetValue,
                            offset
                        )
                        LineRange(
                            BlockLocationImpl(world, minPosition.getX(), minPosition.getY(), z),
                            BlockLocationImpl(world, maxPosition.getX(), maxPosition.getY(), z),
                            targetAxis
                        )
                    }

                    LineAxis.VERTICAL -> throw IllegalArgumentException("수평면에서 수직선을 구할 수 없습니다.")
                }
            }

            PlaneAxis.VERTICAL_X -> {
                when (targetAxis) {
                    LineAxis.HORIZONTAL_X -> {
                        val y = getDot(
                            minPosition.getY(),
                            (minPosition.getY() + maxPosition.getY()) / 2,
                            maxPosition.getY(),
                            offsetValue,
                            offset
                        )
                        LineRange(
                            BlockLocationImpl(world, minPosition.getX(), y, minPosition.getZ()),
                            BlockLocationImpl(world, maxPosition.getX(), y, maxPosition.getZ()),
                            targetAxis
                        )
                    }

                    LineAxis.VERTICAL -> {
                        val z = getDot(
                            minPosition.getZ(),
                            (minPosition.getZ() + maxPosition.getZ()) / 2,
                            maxPosition.getZ(),
                            offsetValue,
                            offset
                        )
                        LineRange(
                            BlockLocationImpl(world, minPosition.getX(), minPosition.getY(), z),
                            BlockLocationImpl(world, maxPosition.getX(), maxPosition.getY(), z),
                            targetAxis
                        )
                    }

                    LineAxis.HORIZONTAL_Z -> throw IllegalArgumentException("X축 수직면에서 Z축 수평선을 구할 수 없습니다.")
                }
            }

            PlaneAxis.VERTICAL_Z -> {
                when (targetAxis) {
                    LineAxis.HORIZONTAL_Z -> {
                        val y = getDot(
                            minPosition.getY(),
                            (minPosition.getY() + maxPosition.getY()) / 2,
                            maxPosition.getY(),
                            offsetValue,
                            offset
                        )
                        LineRange(
                            BlockLocationImpl(world, minPosition.getX(), y, minPosition.getZ()),
                            BlockLocationImpl(world, maxPosition.getX(), y, maxPosition.getZ()),
                            targetAxis
                        )
                    }

                    LineAxis.VERTICAL -> {
                        val x = getDot(
                            minPosition.getX(),
                            (minPosition.getX() + maxPosition.getX()) / 2,
                            maxPosition.getX(),
                            offsetValue,
                            offset
                        )
                        LineRange(
                            BlockLocationImpl(world, x, minPosition.getY(), minPosition.getZ()),
                            BlockLocationImpl(world, x, maxPosition.getY(), maxPosition.getZ()),
                            targetAxis
                        )
                    }

                    LineAxis.HORIZONTAL_X -> throw IllegalArgumentException("Z축 수직면에서 X축 수평선을 구할 수 없습니다.")
                }
            }
        }
    }
}