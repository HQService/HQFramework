package kr.hqservice.framework.nms.v21_11.service.math

import kr.hqservice.framework.nms.service.math.NmsVector3fService
import kr.hqservice.framework.nms.v21_11.wrapper.math.Vector3fWrapperImpl
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import net.minecraft.core.Rotations
import kotlin.reflect.KClass

class NmsVector3fServiceImpl : NmsVector3fService {
    override fun wrap(target: Triple<Float, Float, Float>): Vector3fWrapper {
        val vector3f = Rotations(target.first, target.second, target.third)
        return Vector3fWrapperImpl(vector3f)
    }

    override fun unwrap(wrapper: Vector3fWrapper): Triple<Float, Float, Float> {
        return Triple(wrapper.getX(), wrapper.getY(), wrapper.getZ())
    }

    override fun getOriginalClass(): KClass<*> {
        return Triple::class
    }

    override fun getTargetClass(): KClass<*> {
        return Rotations::class
    }
}