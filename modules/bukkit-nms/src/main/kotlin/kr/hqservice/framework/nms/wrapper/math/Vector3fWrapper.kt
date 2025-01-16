package kr.hqservice.framework.nms.wrapper.math

import kr.hqservice.framework.nms.wrapper.NmsWrapper

interface Vector3fWrapper : NmsWrapper {
    fun getX(): Float
    fun getY(): Float
    fun getZ(): Float
}