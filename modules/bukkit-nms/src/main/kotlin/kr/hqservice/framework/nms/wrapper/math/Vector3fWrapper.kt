package kr.hqservice.framework.nms.wrapper.math

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import net.minecraft.core.Rotations

class Vector3fWrapper(
    private val vector3f: Rotations
) : NmsWrapper {

    fun getX(): Float = vector3f.x
    fun getY(): Float = vector3f.y
    fun getZ(): Float = vector3f.z

    override fun getUnwrappedInstance(): Any {
        return vector3f
    }
}