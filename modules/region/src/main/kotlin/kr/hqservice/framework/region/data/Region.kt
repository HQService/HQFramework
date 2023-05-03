package kr.hqservice.framework.region.data

import kr.hqservice.framework.region.location.RegionCenter

class Region(
    var id: Int,
    var name: String,
    val center: RegionCenter
) {

}