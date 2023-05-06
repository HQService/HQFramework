package kr.hqservice.framework.region.factory

import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.range.Range

interface RangeFactory {

    fun makeRange(position1: BlockLocation, position2: BlockLocation): Range

}