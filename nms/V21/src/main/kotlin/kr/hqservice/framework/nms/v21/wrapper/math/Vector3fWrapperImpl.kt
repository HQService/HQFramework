package kr.hqservice.framework.nms.v21.wrapper.math

import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import net.minecraft.core.Rotations

class Vector3fWrapperImpl(
    private val vector3f: Rotations
) : Vector3fWrapper() {

    override fun getX(): Float = vector3f.x
    override fun getY(): Float = vector3f.y
    override fun getZ(): Float = vector3f.z

    override fun getUnwrappedInstance(): Any {
        return vector3f
    }
}