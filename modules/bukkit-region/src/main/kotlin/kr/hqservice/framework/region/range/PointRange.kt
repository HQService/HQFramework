package kr.hqservice.framework.region.range

import kr.hqservice.framework.region.location.BlockLocation

class PointRange internal constructor(
    position: BlockLocation
) : Range(position, position) {
    override fun getCenter(): BlockLocation {
        return minPosition.clone()
    }
}