package kr.hqservice.framework.nms.service.entity

import kr.hqservice.framework.nms.service.NmsEntityService
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper

interface NmsArmorStandService : NmsEntityService<NmsArmorStandWrapper> {
    fun getHeadPose(wrapper: NmsArmorStandWrapper): Vector3fWrapper

    fun setHeadPose(wrapper: NmsArmorStandWrapper, triple: Triple<Float, Float, Float>)

    fun setSmall(wrapper: NmsArmorStandWrapper, small: Boolean)

    fun setMarker(wrapper: NmsArmorStandWrapper, marker: Boolean)

    fun setArms(wrapper: NmsArmorStandWrapper, arms: Boolean)

    fun setBasePlate(wrapper: NmsArmorStandWrapper, basePlate: Boolean)
}