package kr.hqservice.framework.region.factory.impl

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQService
import kr.hqservice.framework.core.component.Singleton
import kr.hqservice.framework.region.factory.RangeFactory
import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.range.*
import kr.hqservice.framework.region.range.enums.LineAxis
import kr.hqservice.framework.region.range.enums.PlaneAxis
import kotlin.math.max
import kotlin.math.min

@Component
@Singleton(binds = [RangeFactory::class])
class RangeFactoryImpl : RangeFactory, HQService {
    private fun min(position1: BlockLocation, position2: BlockLocation): BlockLocation {
        return BlockLocationImpl(
            position1.getWorld(),
            min(position1.getX(), position2.getX()),
            min(position1.getY(), position2.getY()),
            min(position1.getZ(), position2.getZ())
        )
    }

    private fun max(position1: BlockLocation, position2: BlockLocation): BlockLocation {
        return BlockLocationImpl(
            position1.getWorld(),
            max(position1.getX(), position2.getX()),
            max(position1.getY(), position2.getY()),
            max(position1.getZ(), position2.getZ())
        )
    }

    override fun makeRange(position1: BlockLocation, position2: BlockLocation): Range {
        if (position1.getWorld() != position2.getWorld())
            throw IllegalArgumentException("Other world locations cannot create ranges.")

        if(position1 == position2) return PointRange(position1)

        val minPosition = min(position1, position2)
        val maxPosition = max(position1, position2)

        val matchedX = minPosition.getX() == maxPosition.getX()
        val matchedY = minPosition.getY() == maxPosition.getY()
        val matchedZ = minPosition.getZ() == maxPosition.getZ()

        return when ((if(matchedX) 1 else 0) + (if(matchedY) 1 else 0) + (if(matchedZ) 1 else 0)) {
            3 -> PointRange(minPosition)
            2 -> when {
                !matchedX -> LineRange(minPosition, maxPosition, LineAxis.HORIZONTAL_Z)
                !matchedZ -> LineRange(minPosition, maxPosition, LineAxis.HORIZONTAL_X)
                else -> LineRange(minPosition, maxPosition, LineAxis.VERTICAL)
            }
            1 -> when {
                matchedX -> PlaneRange(minPosition, maxPosition, PlaneAxis.VERTICAL_X)
                matchedZ -> PlaneRange(minPosition, maxPosition, PlaneAxis.VERTICAL_Z)
                else -> PlaneRange(minPosition, maxPosition, PlaneAxis.HORIZONTAL)
            }
            else -> DimensionRange(minPosition, maxPosition)
        }
    }
}