package kr.hqservice.framework.nms.wrapper.math

import kr.hqservice.framework.nms.wrapper.NmsWrapper

abstract class Vector3fWrapper : NmsWrapper {
    abstract fun getX(): Float
    abstract fun getY(): Float
    abstract fun getZ(): Float
}