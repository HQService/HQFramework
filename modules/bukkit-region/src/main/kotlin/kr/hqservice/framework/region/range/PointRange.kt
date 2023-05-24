package kr.hqservice.framework.region.range

import kr.hqservice.framework.region.location.BlockLocation

class PointRange(
    position: BlockLocation
) : Range(position, position) {
    override fun getCenter(): BlockLocation {
        return minPosition.clone()
    }
}