package kr.hqservice.framework.nms.wrapper.entity

import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.wrapper.EntityWrapper

class NmsArmorStandWrapper(
    private val baseEntity: Any,
    private val service: NmsArmorStandService
) : EntityWrapper {
    fun getHeadPose(): Triple<Float, Float, Float> {
        val vector3f = service.getHeadPose(this)
        return Triple(vector3f.getX(), vector3f.getY(), vector3f.getZ())
    }

    fun setHeadPose(triple: Triple<Float, Float, Float>) {
        service.setHeadPose(this, triple)
    }

    fun setSmall(small: Boolean) {
        service.setSmall(this, small)
    }

    fun setMarker(marker: Boolean) {
        service.setMarker(this, marker)
    }

    override fun getUnwrappedInstance(): Any {
        return baseEntity
    }
}